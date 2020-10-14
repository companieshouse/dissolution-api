package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CompanyType {
    PLC("plc"),
    LTD("ltd"),
    LLP("llp"),
    PRIVATE_UNLIMITED("private-unlimited"),
    OLD_PUBLIC_COMPANY("old-public-company"),
    PRIVATE_LIMITED_GUARANT_NSC_LIMITED_EXEMPTION("private-limited-guarant-nsc-limited-exemption"),
    PRIVATE_LIMITED_GUARANT_NSC("private-limited-guarant-nsc"),
    PRIVATE_UNLIMITED_NSC("private-unlimited-nsc"),
    PRIVATE_LIMITED_SHARES_SECTION_30_EXEMPTION("private-limited-shares-section-30-exemption"),
    NORTHERN_IRELAND("northern-ireland"),
    NORTHERN_IRELAND_OTHER("northern-ireland-other"),
    EEIG("european-economic-interest-grouping");

    private final String value;

    CompanyType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
