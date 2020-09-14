package uk.gov.companieshouse.service.dissolution.certificate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionCertificateMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.dto.documentRender.DissolutionCertificateData;
import uk.gov.companieshouse.client.DocumentRenderClient;

@Service
public class DissolutionCertificateGenerator {

    private final DissolutionCertificateMapper mapper;
    private final DissolutionCertificateLocationGenerator locationGenerator;
    private final DocumentRenderClient client;
    private final Logger logger;

    @Autowired
    public DissolutionCertificateGenerator(
            DissolutionCertificateMapper mapper,
            DissolutionCertificateLocationGenerator locationGenerator,
            DocumentRenderClient client,
            Logger logger) {
        this.mapper = mapper;
        this.locationGenerator = locationGenerator;
        this.client = client;
        this.logger = logger;
    }

    public DissolutionCertificate generateDissolutionCertificate(Dissolution dissolution) {
        final DissolutionCertificateData data = mapper.mapToCertificateData(dissolution);

        final String location = locationGenerator.generateCertificateLocation();

        final String savedLocation = client.generateAndStoreDocument(data, getTemplateName(dissolution), location);

        logger.info(String.format("Generated dissolution certificate is available at %s", savedLocation));

        return mapper.mapToDissolutionCertificate(savedLocation);
    }

    private String getTemplateName(Dissolution dissolution) {
        return String.format("%s.html", dissolution.getData().getApplication().getType().getValue());
    }
}
