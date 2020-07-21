package uk.gov.companieshouse.exception.dissolution;

public class DirectorNotPendingApprovalException extends RuntimeException {
    public DirectorNotPendingApprovalException() {
        super("Director is not pending-approval");
    }
}
