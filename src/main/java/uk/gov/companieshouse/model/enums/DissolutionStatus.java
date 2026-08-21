package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DissolutionStatus {
    DRAFT("draft"),
    PENDING("pending"),
    SUBMITTED("submitted");

    private final String value;

    DissolutionStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
