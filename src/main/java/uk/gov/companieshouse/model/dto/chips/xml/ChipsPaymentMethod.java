package uk.gov.companieshouse.model.dto.chips.xml;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import java.util.Map;

public enum ChipsPaymentMethod {

    CREDIT_CARD("creditcard");

    private static final Map<PaymentMethod, ChipsPaymentMethod> PAYMENT_METHOD_MAPPING = Map.of(
            PaymentMethod.CREDIT_CARD, ChipsPaymentMethod.CREDIT_CARD
    );

    private final String value;

    ChipsPaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ChipsPaymentMethod findByDissolutionPaymentMethod(PaymentMethod method) {
        return PAYMENT_METHOD_MAPPING.get(method);
    }

}
