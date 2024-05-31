package uk.gov.companieshouse.service.aws;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import uk.gov.companieshouse.logging.Logger;

import java.util.Arrays;

@Service
public class S3Service {

    private final S3Client s3;

    private final Logger logger;

    public S3Service(S3Client s3, Logger logger) {
        this.s3 = s3;
        this.logger = logger;
    }

    public byte[] downloadFile(String bucket, String key) {
        byte[] downloadFileResponse = s3
                .getObject(
                        GetObjectRequest.builder().bucket(bucket).key(key).build(),
                        ResponseTransformer.toBytes()
                )
                .asByteArray();

        logger.info("S3Service downloadFile response: " + Arrays.toString(downloadFileResponse));

        return downloadFileResponse;
    }
}
