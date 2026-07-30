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

    @JsonProperty("links")
    private List<FilingAttachmentLink> links;

    @JsonProperty("sign_date")
    private String signDate;

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

    public List<FilingAttachmentLink> getLinks() {
        return links;
    }

    public void setLinks(List<FilingAttachmentLink> links) {
        this.links = links;
    }

    public String getSignDate() {
        return signDate;
    }

    public void setSignDate(String signDate) {
        this.signDate = signDate;
    }
}
