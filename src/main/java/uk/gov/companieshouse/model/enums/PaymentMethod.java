package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {
    ACCOUNT("account"),
    CREDIT_CARD("credit-card");

    private final String value;

    PaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
