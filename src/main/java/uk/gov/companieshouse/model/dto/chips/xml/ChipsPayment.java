package uk.gov.companieshouse.model.dto.chips.xml;

public class ChipsPayment {

    private String referenceNumber;
    private ChipsPaymentMethod paymentMethod;

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public ChipsPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(ChipsPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
