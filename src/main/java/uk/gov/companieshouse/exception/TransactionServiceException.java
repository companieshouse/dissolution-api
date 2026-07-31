package uk.gov.companieshouse.exception;

/**
 * Transaction service query failed.
 */
public class TransactionServiceException extends RuntimeException {
    public TransactionServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
