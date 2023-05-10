package uk.gov.companieshouse.model.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentDetailsResponse {
    @JsonProperty("card_type")
    private String cardType;

    @JsonProperty("external_payment_id")
    private String externalPaymentID;

    @JsonProperty("transaction_date")
    private String transactionDate;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("provider_id")
    private String providerID;


    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getExternalPaymentID() {
        return externalPaymentID;
    }

    public void setExternalPaymentID(String externalPaymentID) {
        this.externalPaymentID = externalPaymentID;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getProviderID() {
        return providerID;
    }

    public void setProviderID(String providerID) {
        this.providerID = providerID;
    }
}
