package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CompanyOverseasPrefix {
    FC("FC"),
    NF("NF"),
    SF("SF");

    private final String value;

    CompanyOverseasPrefix(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
