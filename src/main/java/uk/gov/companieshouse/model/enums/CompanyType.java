package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CompanyType {
    PLC("plc"),
    LTD("ltd"),
    EEIG("European economic interest grouping (EEIG)");

    private final String value;

    CompanyType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
