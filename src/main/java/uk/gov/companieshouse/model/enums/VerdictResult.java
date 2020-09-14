package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VerdictResult {
    ACCEPTED(0),
    REJECTED(1);

    private final int value;

    VerdictResult(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }
}
