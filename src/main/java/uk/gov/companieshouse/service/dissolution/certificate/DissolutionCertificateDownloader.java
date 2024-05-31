package uk.gov.companieshouse.service.dissolution.certificate;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.service.aws.S3Service;

@Service
public class DissolutionCertificateDownloader {

    private final S3Service s3;

    private final Logger logger;

    public DissolutionCertificateDownloader(S3Service s3, Logger logger) {
        this.s3 = s3;
        this.logger = logger;
    }

    public byte[] downloadDissolutionCertificate(Dissolution dissolution) {
        final DissolutionCertificate certificate = dissolution.getCertificate();

        logger.info("DissolutionCertificateDownloader getBucket: " + certificate.getBucket());
        logger.info("DissolutionCertificateDownloader getKey: " + certificate.getKey());

        return s3.downloadFile(certificate.getBucket(), certificate.getKey());
    }
}
