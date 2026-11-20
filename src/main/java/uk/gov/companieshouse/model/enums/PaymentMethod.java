package uk.gov.companieshouse.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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

    public static PaymentMethod fromValue(String value) {
        return Arrays.stream(values())
                .filter(m -> m.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment method: " + value));
    }
}
