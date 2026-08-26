package uk.gov.companieshouse.service.dissolution.certificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.config.DissolutionConfig;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;
import static uk.gov.companieshouse.model.Constants.CERTIFICATE_FILE_NAME_PREFIX;
import static uk.gov.companieshouse.model.Constants.S3_URI_PATTERN;

@ExtendWith(MockitoExtension.class)
class DissolutionCertificateLocationGeneratorTest {

    private static final String APPLICATION_REFERENCE = "DEF456";
    private static final String S3_BUCKET_NAME = "some-pdf-bucket";
    private static final String ENV_NAME = "some-env";

    @InjectMocks
    private DissolutionCertificateLocationGenerator locationGenerator;

    @Mock
    private DissolutionConfig dissolutionConfig;

    @Mock
    private EnvironmentConfig envConfig;

    @Test
    void generateCertificateLocation_generatesAnS3UrlUsingThePdfBucketAndEnvName() {
        final Dissolution dissolution = generateDissolution();
        dissolution.getData().getApplication().setReference(APPLICATION_REFERENCE);

        when(dissolutionConfig.getDissolutionPdfBucket()).thenReturn(S3_BUCKET_NAME);
        when(envConfig.getEnvironmentName()).thenReturn(ENV_NAME);

        final String result = locationGenerator.generateCertificateLocation(dissolution);

        String expectedLocation = String.format(S3_URI_PATTERN, S3_BUCKET_NAME, ENV_NAME, CERTIFICATE_FILE_NAME_PREFIX, APPLICATION_REFERENCE);
        assertEquals(expectedLocation, result);
    }

    @Test
    void generateCertificateLocation_generatesAnS3UrlUsingTheTransactionId() {
        final Dissolution dissolution = DissolutionTestDataBuilder.aDissolution().withTransactionId(TRANSACTION_ID).withStatus(DissolutionStatus.PENDING).build();

        when(dissolutionConfig.getDissolutionPdfBucket()).thenReturn(S3_BUCKET_NAME);
        when(envConfig.getEnvironmentName()).thenReturn(ENV_NAME);

        final String result = locationGenerator.generateCertificateLocation(dissolution);

        String expectedLocation = String.format(S3_URI_PATTERN, S3_BUCKET_NAME, ENV_NAME, CERTIFICATE_FILE_NAME_PREFIX, TRANSACTION_ID);
        assertEquals(expectedLocation, result);
    }
}
