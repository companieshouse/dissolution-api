package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CompanyStatus {
    ACTIVE("active"),
    DISSOLVED("dissolved");

    private final String value;

    CompanyStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
