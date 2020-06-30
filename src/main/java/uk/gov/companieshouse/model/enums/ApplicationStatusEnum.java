package uk.gov.companieshouse.model.enums;

public enum ApplicationStatusEnum {
 PENDING_APPROVAL("pending-approval"),
 PENDING_PAYMENT("pending-payment"),
 PAID("paid");

    public final String label;

    ApplicationStatusEnum(String label) {
        this.label = label;
    }
}
