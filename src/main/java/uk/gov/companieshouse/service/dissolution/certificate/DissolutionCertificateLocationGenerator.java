package uk.gov.companieshouse.service.dissolution.certificate;

import org.apache.commons.lang3.StringUtils;
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
        final String reference = StringUtils.isBlank(dissolution.getTransactionId())
                ? dissolution.getData().getApplication().getReference()
                : dissolution.getTransactionId();
        return String.format(S3_URI_PATTERN, dissolutionConfig.getDissolutionPdfBucket(), envConfig.getEnvironmentName(), CERTIFICATE_FILE_NAME_PREFIX, reference);
    }
}
