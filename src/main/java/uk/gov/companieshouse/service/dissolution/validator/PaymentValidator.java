package uk.gov.companieshouse.service.dissolution.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import java.util.Optional;

@Service
public class PaymentValidator {

    private static final String ERROR_CANNOT_SUBMIT_BOTH_PAYMENT_REFERENCE_AND_ACCOUNT_NUMBER = "You cannot submit both a payment reference number and an account number";
    private static final String ERROR_PAYMENT_REFERENCE_CANNOT_BE_EMPTY_FOR_CARD_PAYMENT = "You must provide a payment reference number for a card payment";
    private static final String ERROR_ACCOUNT_NUMBER_CANNOT_BE_EMPTY_FOR_ACCOUNT_PAYMENT = "You must provide an account number to pay by account";

    @Autowired
    public PaymentValidator() {}

    public Optional<String> checkBusinessRules(PaymentPatchRequest body) {
        if (isPaymentReferenceAndAccountNumberProvided(body)) {
            return Optional.of(ERROR_CANNOT_SUBMIT_BOTH_PAYMENT_REFERENCE_AND_ACCOUNT_NUMBER);
        }

        if (isPaymentReferenceNotProvidedForCardPayment(body)) {
            return Optional.of(ERROR_PAYMENT_REFERENCE_CANNOT_BE_EMPTY_FOR_CARD_PAYMENT);
        }

        if (isAccountNumberNotProvidedForAccountPayment(body)) {
            return Optional.of(ERROR_ACCOUNT_NUMBER_CANNOT_BE_EMPTY_FOR_ACCOUNT_PAYMENT);
        }

        return Optional.empty();
    }

    private boolean isPaymentReferenceAndAccountNumberProvided(PaymentPatchRequest body) {
        return  !StringUtils.isEmpty(body.getPaymentReference()) &&
                !StringUtils.isEmpty(body.getAccountNumber());
    }

    private boolean isPaymentReferenceNotProvidedForCardPayment(PaymentPatchRequest body) {
        return body.getPaymentMethod() == PaymentMethod.CREDIT_CARD && StringUtils.isEmpty(body.getPaymentReference());
    }

    private boolean isAccountNumberNotProvidedForAccountPayment(PaymentPatchRequest body) {
        return  body.getPaymentMethod() == PaymentMethod.ACCOUNT &&
                StringUtils.isEmpty(body.getAccountNumber());
    }
}
