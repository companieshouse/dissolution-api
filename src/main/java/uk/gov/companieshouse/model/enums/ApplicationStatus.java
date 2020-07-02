package uk.gov.companieshouse.model.enums;

public enum ApplicationStatus {
    PENDING_APPROVAL("pending-approval"),
    PENDING_PAYMENT("pending-payment"),
    PAID("paid");

    private final String value;

    ApplicationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
