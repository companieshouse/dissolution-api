package uk.gov.companieshouse.service.dissolution.certificate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.service.aws.S3Service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionCertificate;

@ExtendWith(MockitoExtension.class)
public class DissolutionCertificateDownloaderTest {

    @InjectMocks
    private DissolutionCertificateDownloader downloader;

    @Mock
    private S3Service s3;

    @Test
    public void downloadDissolutionCertificate_downloadsFromS3UsingDissolutionCertificateKeyAndBucket() {
        final byte[] certificateContents = "some certificate".getBytes();

        final Dissolution dissolution = generateDissolution();

        final DissolutionCertificate certificate = generateDissolutionCertificate();
        certificate.setBucket("some-cert-bucket");
        certificate.setKey("some-cert-key.pdf");

        dissolution.setCertificate(certificate);

        when(s3.downloadFile("some-cert-bucket", "some-cert-key.pdf")).thenReturn(certificateContents);

        final byte[] result = downloader.downloadDissolutionCertificate(dissolution);

        assertEquals(certificateContents, result);

        verify(s3).downloadFile("some-cert-bucket", "some-cert-key.pdf");
    }
}
