package uk.gov.companieshouse.service.dissolution.chips;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.ChipsClient;
import uk.gov.companieshouse.config.ChipsConfig;
import uk.gov.companieshouse.exception.ChipsNotAvailableException;
import uk.gov.companieshouse.mapper.chips.DissolutionChipsMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;
import uk.gov.companieshouse.model.enums.SubmissionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateDownloader;

import java.time.LocalDateTime;

@Service
public class ChipsSubmitter {

    private final Logger logger = LoggerFactory.getLogger(ChipsSubmitter.class);

    private final DissolutionCertificateDownloader certificateDownloader;
    private final DissolutionChipsMapper mapper;
    private final ChipsClient client;
    private final ChipsConfig config;
    private final DissolutionRepository repository;

    public ChipsSubmitter(
            DissolutionCertificateDownloader certificateDownloader,
            DissolutionChipsMapper mapper,
            ChipsClient client,
            ChipsConfig config,
            DissolutionRepository repository) {
        this.certificateDownloader = certificateDownloader;
        this.mapper = mapper;
        this.client = client;
        this.config = config;
        this.repository = repository;
    }

    public void submitDissolutionToChips(Dissolution dissolution) {
        boolean wasSuccessful = sendToChips(dissolution);

        updateDissolutionWithResult(dissolution, wasSuccessful);
    }

    private boolean sendToChips(Dissolution dissolution) {
        final String companyNumber = dissolution.getCompany().getNumber();

        logger.info("Sending dissolution request to CHIPS for company {}", companyNumber);

        final byte[] certificateContents = certificateDownloader.downloadDissolutionCertificate(dissolution);

        try {
            final DissolutionChipsRequest dissolutionRequest = mapper.mapToDissolutionChipsRequest(dissolution, certificateContents);

            client.sendDissolutionToChips(dissolutionRequest);

            logger.info("Dissolution request successfully sent to CHIPS for company {}", companyNumber);

            return true;
        } catch (ChipsNotAvailableException ex) {
            logger.info("CHIPS was not available when submitting for company {}", companyNumber);
        } catch (RuntimeException ex) {
            logger.error("Unexpected error thrown when submitting to CHIPS for company {}", companyNumber, ex);
        }

        return false;
    }

    private void updateDissolutionWithResult(Dissolution dissolution, boolean wasSuccessful) {
        final DissolutionSubmission submission = dissolution.getSubmission();
        submission.setDateTime(LocalDateTime.now());

        if (wasSuccessful) {
            submission.setStatus(SubmissionStatus.SENT);
        } else {
            handleFailedSubmission(submission);
        }

        repository.save(dissolution);
    }

    private void handleFailedSubmission(DissolutionSubmission submission) {
        submission.setRetryCounter(submission.getRetryCounter() + 1);

        if (submission.getRetryCounter() == config.getChipsRetryLimit()) {
            submission.setStatus(SubmissionStatus.FAILED);
        }
    }
}
