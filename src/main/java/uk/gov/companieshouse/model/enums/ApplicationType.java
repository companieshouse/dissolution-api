package uk.gov.companieshouse.model.enums;

public enum ApplicationType {
    DS01("ds01"),
    LLDS01("llds01");

    private final String value;

    ApplicationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
