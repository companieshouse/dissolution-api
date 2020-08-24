package uk.gov.companieshouse.service.dissolution.certificate;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.service.aws.S3Service;

@Service
public class DissolutionCertificateDownloader {

    private final S3Service s3;

    public DissolutionCertificateDownloader(S3Service s3) {
        this.s3 = s3;
    }

    public byte[] downloadDissolutionCertificate(Dissolution dissolution) {
        final DissolutionCertificate certificate = dissolution.getCertificate();

        return s3.downloadFile(certificate.getBucket(), certificate.getKey());
    }
}
