package uk.gov.companieshouse.exception;

public class DissolutionNotLinkedToTransactionException extends RuntimeException {

    public DissolutionNotLinkedToTransactionException(String message) {
        super(message);
    }
}
