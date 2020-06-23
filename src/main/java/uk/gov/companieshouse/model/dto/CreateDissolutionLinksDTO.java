package uk.gov.companieshouse.model.dto;

public class CreateDissolutionLinksDTO {

    private String self;
    private String payment;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }
}
