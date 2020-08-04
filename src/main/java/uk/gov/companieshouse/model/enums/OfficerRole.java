package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OfficerRole {
    DIRECTOR("director"),
    SECRETARY("secretary");

    private final String value;

    OfficerRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
