package uk.gov.companieshouse.exception;

public class DissolutionDirectorNotFoundException extends RuntimeException {
    public DissolutionDirectorNotFoundException() {
        super();
    }

    public DissolutionDirectorNotFoundException(String message) {
        super(message);
    }
}
