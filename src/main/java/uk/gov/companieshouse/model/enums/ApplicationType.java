package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationType {
    DS01("ds01"),
    LLDS01("llds01");

    private final String value;

    ApplicationType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
