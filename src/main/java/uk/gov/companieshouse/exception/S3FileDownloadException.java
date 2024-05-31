package uk.gov.companieshouse.exception;

public class S3FileDownloadException extends RuntimeException {
    public S3FileDownloadException(String message) {
        super(message);
    }

    public S3FileDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}