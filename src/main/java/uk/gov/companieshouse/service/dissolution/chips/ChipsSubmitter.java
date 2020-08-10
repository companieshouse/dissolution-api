package uk.gov.companieshouse.service.dissolution.chips;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.ChipsClient;
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
    private final DissolutionRepository repository;

    public ChipsSubmitter(
            DissolutionCertificateDownloader certificateDownloader,
            DissolutionChipsMapper mapper,
            ChipsClient client,
            DissolutionRepository repository) {
        this.certificateDownloader = certificateDownloader;
        this.mapper = mapper;
        this.client = client;
        this.repository = repository;
    }

    public void submitDissolutionToChips(Dissolution dissolution) {
        boolean wasSuccessful = sendToChips(dissolution);

        updateDissolutionWithResult(dissolution, wasSuccessful);
    }

    private boolean sendToChips(Dissolution dissolution) {
        final String certificateContents = certificateDownloader.downloadDissolutionCertificate(dissolution);

        try {
            final DissolutionChipsRequest dissolutionRequest = mapper.mapToDissolutionChipsRequest(dissolution, certificateContents);

            logger.info("Sending dissolution request to CHIPS for company {}", dissolution.getCompany().getNumber());
            logger.info("{}", asJsonString(dissolutionRequest));

            client.sendDissolutionToChips(dissolutionRequest);

            return true;
        } catch (JsonProcessingException ex) {
            logger.error("Failed to map to CHIPS request", ex);
        } catch (ChipsNotAvailableException ex) {
            logger.info("CHIPS is not available");
        }

        return false;
    }

    private void updateDissolutionWithResult(Dissolution dissolution, boolean wasSuccessful) {
        final DissolutionSubmission submission = dissolution.getSubmission();
        submission.setDateTime(LocalDateTime.now());

        if (wasSuccessful) {
            submission.setStatus(SubmissionStatus.SENT);
        }

        repository.save(dissolution);
    }

    private String asJsonString(DissolutionChipsRequest data) {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to write dissolution certificate data to JSON string");
        }
    }
}
