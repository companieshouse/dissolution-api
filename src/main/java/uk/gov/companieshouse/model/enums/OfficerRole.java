package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum OfficerRole {
    DIRECTOR("director"),
    CORPORATE_DIRECTOR("corporate-director"),
    CORPORATE_NOMINEE_DIRECTOR("corporate-nominee-director"),
    JUDICIAL_FACTOR("judicial-factor"),
    SECRETARY("secretary"),
    LLP_MEMBER("llp-member"),
    LLP_DESIGNATED_MEMBER("llp-designated-member"),
    CORPORATE_LLP_MEMBER("corporate-llp-member"),
    CORPORATE_LLP_DESIGNATED_MEMBER("corporate-llp-designated-member");

    private final String value;

    OfficerRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
