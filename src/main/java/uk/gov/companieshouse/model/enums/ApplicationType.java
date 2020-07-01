package uk.gov.companieshouse.model.enums;

public enum ApplicationType {
    DS01("ds01"),
    LLDS01("llds01");

    public final String label;

    ApplicationType(String label) {
        this.label = label;
    }
}
