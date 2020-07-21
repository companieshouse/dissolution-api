package uk.gov.companieshouse.exception.dissolution;

public class MissingEricHeadersException extends RuntimeException {
    public MissingEricHeadersException() {
        super("Eric headers are missing");
    }
}
