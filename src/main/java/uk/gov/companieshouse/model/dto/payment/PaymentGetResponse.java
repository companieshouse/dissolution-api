package uk.gov.companieshouse.model.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentGetResponse {
    @JsonProperty("ETag")
    private String eTag;

    private String kind;

    private PaymentLinks links;

    @JsonProperty("company_number")
    private String companyNumber;

    private List<PaymentItem> items;

    @JsonProperty("ETag")
    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public PaymentLinks getLinks() {
        return links;
    }

    public void setLinks(PaymentLinks links) {
        this.links = links;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public List<PaymentItem> getItems() {
        return items;
    }

    public void setItems(List<PaymentItem> items) {
        this.items = items;
    }
}
