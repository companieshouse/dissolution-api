package uk.gov.companieshouse.exception;

public class DissolutionDirectorApprovalException extends RuntimeException {
    public DissolutionDirectorApprovalException() {
        super();
    }

    public DissolutionDirectorApprovalException(String message) {
        super(message);
    }
}
