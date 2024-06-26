package uk.gov.companieshouse.exception;

/**
 * Company profile not found or external query failed.
 */
public class CompanyProfileServiceException extends RuntimeException {

    public CompanyProfileServiceException(final String message) {
        super(message);
    }

    public CompanyProfileServiceException(final String s, final Exception e) {
        super(s, e);
    }
}
