package uk.gov.companieshouse.exception;

public class DissolutionDirectorApprovalException extends BadRequestException {
    public DissolutionDirectorApprovalException(String message) {
        super(message);
    }
}
