package uk.gov.companieshouse.exception;

public class DissolutionNotFoundException extends RuntimeException {
    public DissolutionNotFoundException() {
        super();
    }

    public DissolutionNotFoundException(String message) {
        super(message);
    }
}
