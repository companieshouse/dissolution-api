package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RefundStatus {
    SUBMITTED("refund-requested"),
    SUCCESS("refund-success"),
    ERROR("refund-error");

    private final String value;

    RefundStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
