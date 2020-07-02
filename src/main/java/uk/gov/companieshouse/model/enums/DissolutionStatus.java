package uk.gov.companieshouse.model.enums;

public enum DissolutionStatus {
    PENDING_APPROVAL("pending-approval"),
    PENDING_PAYMENT("pending-payment"),
    PAID("paid");

    public final String label;

    DissolutionStatus(String label) {
        this.label = label;
    }
}
