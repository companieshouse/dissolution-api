package uk.gov.companieshouse.service.aws;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import uk.gov.companieshouse.exception.S3FileDownloadException;

import java.io.IOException;

@Service
public class S3Service {

    private final S3Client s3;

    public S3Service(S3Client s3) {
        this.s3 = s3;
    }

    public byte[] downloadFile(String bucket, String key) {

        try {
            return IOUtils.toByteArray(s3.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build()));
        } catch (IOException exception) {
            throw new S3FileDownloadException("Failed to convert file to byteArray", exception);
        } catch (SdkException se) {
            throw new S3FileDownloadException("Failed to download file", se);
        }
    }
}
