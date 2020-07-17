package uk.gov.companieshouse.model.dto.payment;


import uk.gov.companieshouse.model.enums.PaymentStatus;

import java.sql.Timestamp;

public class PaymentPatchRequest {
    private PaymentStatus status;

    private String paymentReference;

    private Timestamp paidAt;

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }
}
