package uk.gov.companieshouse.service.dissolution.chips;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.ChipsClient;
import uk.gov.companieshouse.mapper.chips.DissolutionChipsMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateDownloader;

@Service
public class ChipsSubmitter {

    private final Logger logger = LoggerFactory.getLogger(ChipsSubmitter.class);

    private final DissolutionCertificateDownloader certificateDownloader;
    private final DissolutionChipsMapper mapper;
    private final ChipsClient client;

    public ChipsSubmitter(
            DissolutionCertificateDownloader certificateDownloader,
            DissolutionChipsMapper mapper,
            ChipsClient client) {
        this.certificateDownloader = certificateDownloader;
        this.mapper = mapper;
        this.client = client;
    }

    public void submitDissolutionToChips(Dissolution dissolution) {
        final String certificateContents = certificateDownloader.downloadDissolutionCertificate(dissolution);

        try {
            final DissolutionChipsRequest dissolutionRequest = mapper.mapToDissolutionChipsRequest(dissolution, certificateContents);

            logger.info("Sending dissolution request to CHIPS for company {}", dissolution.getCompany().getNumber());
            logger.info("{}", asJsonString(dissolutionRequest));

            client.sendDissolutionToChips(dissolutionRequest);
        } catch (JsonProcessingException ex) {
            logger.error("Failed to map to CHIPS request", ex);
        }

        // TODO - 4 Update database
    }

    private String asJsonString(DissolutionChipsRequest data) {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to write dissolution certificate data to JSON string");
        }
    }
}
