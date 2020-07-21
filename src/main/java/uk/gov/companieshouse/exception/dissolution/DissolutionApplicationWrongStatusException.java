package uk.gov.companieshouse.exception.dissolution;

public class DissolutionApplicationWrongStatusException extends RuntimeException {
    public DissolutionApplicationWrongStatusException() {
        super("Dissolution status is not 'pending-payment'");
    }
}
