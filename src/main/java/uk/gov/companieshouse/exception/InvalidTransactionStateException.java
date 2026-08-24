package uk.gov.companieshouse.exception;

public class InvalidTransactionStateException extends ConflictException {
    public InvalidTransactionStateException(String message) {
        super(message);
    }
}
