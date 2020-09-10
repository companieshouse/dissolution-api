package uk.gov.companieshouse.model.dto.email;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    SIGNATORY_TO_SIGN("dissolution_signatory_to_sign"),
    SIGNATORY_TO_SIGN_LLDS01("dissolution_signatory_to_sign_llds01"),
    PENDING_PAYMENT("dissolution_payment_pending"),
    PENDING_PAYMENT_LLDS01("dissolution_payment_pending_llds01"),
    SUCCESSFUL_PAYMENT("dissolution_payment_confirmation"),
    SUCCESSFUL_PAYMENT_LLDS01("dissolution_payment_confirmation_llds01"),
    APPLICATION_ACCEPTED("dissolution_submission_accepted"),
    APPLICATION_ACCEPTED_LLDS01("dissolution_submission_accepted_llds01"),
    APPLICATION_REJECTED("dissolution_submission_rejected"),
    APPLICATION_REJECTED_LLDS01("dissolution_submission_rejected_llds01");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
