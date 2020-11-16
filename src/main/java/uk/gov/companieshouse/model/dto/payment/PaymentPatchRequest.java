package uk.gov.companieshouse.model.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.model.enums.PaymentStatus;

import java.sql.Timestamp;

public class PaymentPatchRequest {
    private PaymentStatus status;

    @JsonProperty("payment_reference")
    private String paymentReference;

    @JsonProperty("paid_at")
    private Timestamp paidAt;

    @JsonProperty("payment_method")
    private PaymentMethod paymentMethod;

    @JsonProperty("account_number")
    private String accountNumber;

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

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
