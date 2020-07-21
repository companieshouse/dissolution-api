package uk.gov.companieshouse.model.dto.payment;


import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.model.enums.PaymentStatus;

import java.sql.Timestamp;

public class PaymentPatchRequest {
    private PaymentStatus status;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("paid_at")
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
