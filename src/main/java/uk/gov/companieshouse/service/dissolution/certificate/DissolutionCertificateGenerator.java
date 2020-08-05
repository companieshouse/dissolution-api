package uk.gov.companieshouse.service.dissolution.certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DissolutionCertificateMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.dto.documentRender.DissolutionCertificateData;
import uk.gov.companieshouse.client.DocumentRenderClient;

@Service
public class DissolutionCertificateGenerator {

    private final Logger logger = LoggerFactory.getLogger(DissolutionCertificateGenerator.class);

    private final DissolutionCertificateMapper mapper;
    private final DissolutionCertificateLocationGenerator locationGenerator;
    private final DocumentRenderClient client;

    @Autowired
    public DissolutionCertificateGenerator(
            DissolutionCertificateMapper mapper,
            DissolutionCertificateLocationGenerator locationGenerator,
            DocumentRenderClient client) {
        this.mapper = mapper;
        this.locationGenerator = locationGenerator;
        this.client = client;
    }

    public DissolutionCertificate generateDissolutionCertificate(Dissolution dissolution) {
        final DissolutionCertificateData data = mapper.mapToCertificateData(dissolution);

        final String location = locationGenerator.generateCertificateLocation();

        final String savedLocation = client.generateAndStoreDocument(data, getTemplateName(dissolution), location);

        logger.info("Generated dissolution certificate is available at {}", savedLocation);

        return mapper.mapToDissolutionCertificate(savedLocation);
    }

    private String getTemplateName(Dissolution dissolution) {
        return String.format("%s.html", dissolution.getData().getApplication().getType().getValue());
    }
}
