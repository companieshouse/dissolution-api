package uk.gov.companieshouse.exception.dissolution;

public class DissolutionNotFoundException extends RuntimeException {
    public DissolutionNotFoundException() {
        super("Dissolution not found");
    }
}
