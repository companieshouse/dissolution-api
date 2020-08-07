package uk.gov.companieshouse.service.dissolution.certificate;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.service.aws.S3Service;

import java.nio.charset.StandardCharsets;

@Service
public class DissolutionCertificateDownloader {

    private final S3Service s3;

    public DissolutionCertificateDownloader(S3Service s3) {
        this.s3 = s3;
    }

    public String downloadDissolutionCertificate(Dissolution dissolution) {
        final DissolutionCertificate certificate = dissolution.getCertificate();

        final byte[] certificateContents = s3.downloadFile(certificate.getBucket(), certificate.getKey());

        return new String(certificateContents, StandardCharsets.UTF_8);
    }
}
