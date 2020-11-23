package uk.gov.companieshouse.model.db.payment;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import java.time.LocalDateTime;

public class PaymentInformation {
    private String reference;

    private PaymentMethod method;

    @Field("date_time")
    private LocalDateTime dateTime;

    @Field("account_number")
    private String accountNumber;

    private RefundInformation refund;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public RefundInformation getRefund() { return refund; }

    public void setRefund(RefundInformation refund) { this.refund = refund; }
}
