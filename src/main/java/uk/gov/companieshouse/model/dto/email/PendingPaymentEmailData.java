package uk.gov.companieshouse.model.dto.email;

public class PendingPaymentEmailData extends EmailData {
    private String dissolutionReferenceNumber;
    private String companyNumber;
    private String companyName;
    private String chsUrl;

    public String getDissolutionReferenceNumber() {
        return dissolutionReferenceNumber;
    }

    public void setDissolutionReferenceNumber(String dissolutionReferenceNumber) {
        this.dissolutionReferenceNumber = dissolutionReferenceNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getChsUrl() {
        return chsUrl;
    }

    public void setChsUrl(String chsUrl) {
        this.chsUrl = chsUrl;
    }
}
