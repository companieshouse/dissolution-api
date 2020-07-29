package uk.gov.companieshouse.service.dissolution.certificate;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.DissolutionConfig;
import uk.gov.companieshouse.config.EnvironmentConfig;

@Service
public class DissolutionCertificateLocationGenerator {

    private final DissolutionConfig dissolutionConfig;
    private final EnvironmentConfig envConfig;

    public DissolutionCertificateLocationGenerator(DissolutionConfig dissolutionConfig, EnvironmentConfig envConfig) {
        this.dissolutionConfig = dissolutionConfig;
        this.envConfig = envConfig;
    }

    public String generateCertificateLocation() {
        return String.format("s3://%s/%s/dissolution", dissolutionConfig.getDissolutionPdfBucket(), envConfig.getEnvironmentName());
    }
}
