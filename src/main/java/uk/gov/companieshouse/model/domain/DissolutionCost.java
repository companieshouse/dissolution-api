package uk.gov.companieshouse.model.domain;

public class DissolutionCost {

    private final String amount;
    private final String companyName;
    private final String companyNumber;
    private final String applicationType;

    public DissolutionCost(String amount, String companyName, String companyNumber, String applicationType) {
        this.amount = amount;
        this.companyName = companyName;
        this.companyNumber = companyNumber;
        this.applicationType = applicationType;
    }

    public String getAmount() {
        return amount;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public String getApplicationType() {
        return applicationType;
    }
}
