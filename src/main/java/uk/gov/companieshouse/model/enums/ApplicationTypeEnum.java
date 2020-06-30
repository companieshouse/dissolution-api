package uk.gov.companieshouse.model.enums;

public enum ApplicationTypeEnum {
    DS01("ds01"),
    LLDS01("llds01");

    public final String label;

    ApplicationTypeEnum(String label) {
        this.label = label;
    }
}
