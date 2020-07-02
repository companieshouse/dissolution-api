package uk.gov.companieshouse.model.enums;

public enum DissolutionType {
    DS01("ds01"),
    LLDS01("llds01");

    public final String label;

    DissolutionType(String label) {
        this.label = label;
    }
}
