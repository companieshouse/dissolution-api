package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationStatus {
    PENDING_APPROVAL("pending-approval"),
    PENDING_PAYMENT("pending-payment"),
    PAID("paid");

    private final String value;

    ApplicationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
