package uk.gov.companieshouse.model.enums;

public enum ApplicationStatus {
    PENDING_APPROVAL("pending-approval"),
    PENDING_PAYMENT("pending-payment"),
    PAID("paid");

    public final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }
}
