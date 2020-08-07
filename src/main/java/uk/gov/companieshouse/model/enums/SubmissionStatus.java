package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SubmissionStatus {
    PENDING("pending"),
    SENT("sent"),
    FAILED("failed");

    private final String value;

    SubmissionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
