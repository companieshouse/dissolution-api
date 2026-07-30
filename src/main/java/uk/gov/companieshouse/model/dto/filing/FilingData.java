package uk.gov.companieshouse.model.dto.filing;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FilingData {

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("officers")
    private List<FilingOfficer> officers;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("payment_method")
    private String paymentMethod;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public List<FilingOfficer> getOfficers() {
        return officers;
    }

    public void setOfficers(List<FilingOfficer> officers) {
        this.officers = officers;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
