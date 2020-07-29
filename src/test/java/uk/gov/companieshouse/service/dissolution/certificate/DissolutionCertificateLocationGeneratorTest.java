package uk.gov.companieshouse.service.dissolution.certificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.config.DissolutionConfig;
import uk.gov.companieshouse.config.EnvironmentConfig;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DissolutionCertificateLocationGeneratorTest {

    @InjectMocks
    private DissolutionCertificateLocationGenerator locationGenerator;

    @Mock
    private DissolutionConfig dissolutionConfig;

    @Mock
    private EnvironmentConfig envConfig;

    @Test
    public void generateCertificateLocation_generatesAnS3UrlUsingThePdfBucketAndEnvName() {
        when(dissolutionConfig.getDissolutionPdfBucket()).thenReturn("some-pdf-bucket");
        when(envConfig.getEnvironmentName()).thenReturn("some-env");

        final String result = locationGenerator.generateCertificateLocation();

        assertEquals("s3://some-pdf-bucket/some-env/dissolution", result);
    }
}
