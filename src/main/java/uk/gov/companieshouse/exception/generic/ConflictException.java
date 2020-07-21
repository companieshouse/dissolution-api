package uk.gov.companieshouse.exception.generic;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
