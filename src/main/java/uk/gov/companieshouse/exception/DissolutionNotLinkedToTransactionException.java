package uk.gov.companieshouse.exception;

public class DissolutionNotLinkedToTransactionException extends BadRequestException {

    public DissolutionNotLinkedToTransactionException(String message) {
        super(message);
    }
}
