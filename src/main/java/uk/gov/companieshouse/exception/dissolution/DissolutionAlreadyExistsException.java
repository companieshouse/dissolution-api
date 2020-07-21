package uk.gov.companieshouse.exception.dissolution;

public class DissolutionAlreadyExistsException extends RuntimeException {
    public DissolutionAlreadyExistsException() {
        super("Dissolution already exists");
    }
}
