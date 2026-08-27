package uk.gov.companieshouse.service.dissolution.certificate;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.DissolutionConfig;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import static uk.gov.companieshouse.model.Constants.CERTIFICATE_FILE_NAME_PREFIX;
import static uk.gov.companieshouse.model.Constants.S3_URI_PATTERN;

@Service
public class DissolutionCertificateLocationGenerator {

    private final DissolutionConfig dissolutionConfig;
    private final EnvironmentConfig envConfig;

    public DissolutionCertificateLocationGenerator(DissolutionConfig dissolutionConfig, EnvironmentConfig envConfig) {
        this.dissolutionConfig = dissolutionConfig;
        this.envConfig = envConfig;
    }

    public String generateCertificateLocation(Dissolution dissolution) {
        return String.format(S3_URI_PATTERN, dissolutionConfig.getDissolutionPdfBucket(), envConfig.getEnvironmentName(), CERTIFICATE_FILE_NAME_PREFIX, dissolution.getReferenceNumber());
    }
}
