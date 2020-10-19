package uk.gov.companieshouse.service.dissolution.certificate;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.DissolutionConfig;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

@Service
public class DissolutionCertificateLocationGenerator {

    private static final String CERTIFICATE_FILE_NAME_PREFIX = "Apply-to-strike-off-and-dissolve-a-company";

    private final DissolutionConfig dissolutionConfig;
    private final EnvironmentConfig envConfig;

    public DissolutionCertificateLocationGenerator(DissolutionConfig dissolutionConfig, EnvironmentConfig envConfig) {
        this.dissolutionConfig = dissolutionConfig;
        this.envConfig = envConfig;
    }

    public String generateCertificateLocation(Dissolution dissolution) {
        return String.format(
                "s3://%s/%s/dissolution/%s-%s.pdf",
                dissolutionConfig.getDissolutionPdfBucket(), envConfig.getEnvironmentName(), CERTIFICATE_FILE_NAME_PREFIX, dissolution.getData().getApplication().getReference()
        );
    }
}
