package uk.gov.companieshouse.model.dto.chips.xml;

public class ChipsPayment {

    private String referenceNumber;
    private ChipsPaymentMethod paymentMethod;
    private String accountNumber;

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

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
