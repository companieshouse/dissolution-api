package uk.gov.companieshouse.exception;

public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException() {
    }

    public ServiceUnavailableException(final String message) {
        super(message);
    }
}
