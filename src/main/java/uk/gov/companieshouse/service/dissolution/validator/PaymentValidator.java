package uk.gov.companieshouse.service.dissolution.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.domain.PaymentDetails;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import java.util.Optional;

@Service
public class PaymentValidator {

    private static final String ERROR_CANNOT_SUBMIT_BOTH_PAYMENT_REFERENCE_AND_ACCOUNT_NUMBER = "You cannot submit both a payment reference number and an account number";
    private static final String ERROR_PAYMENT_REFERENCE_CANNOT_BE_EMPTY_FOR_CARD_PAYMENT = "You must provide a payment reference number for a card payment";
    private static final String ERROR_ACCOUNT_NUMBER_CANNOT_BE_EMPTY_FOR_ACCOUNT_PAYMENT = "You must provide an account number to pay by account";

    public Optional<String> checkBusinessRules(PaymentDetails details) {
        if (isPaymentReferenceAndAccountNumberProvided(details)) {
            return Optional.of(ERROR_CANNOT_SUBMIT_BOTH_PAYMENT_REFERENCE_AND_ACCOUNT_NUMBER);
        }

        if (isPaymentReferenceNotProvidedForCardPayment(details)) {
            return Optional.of(ERROR_PAYMENT_REFERENCE_CANNOT_BE_EMPTY_FOR_CARD_PAYMENT);
        }

        if (isAccountNumberNotProvidedForAccountPayment(details)) {
            return Optional.of(ERROR_ACCOUNT_NUMBER_CANNOT_BE_EMPTY_FOR_ACCOUNT_PAYMENT);
        }

        return Optional.empty();
    }

    private boolean isPaymentReferenceAndAccountNumberProvided(PaymentDetails details) {
        return  !StringUtils.isEmpty(details.getPaymentReference()) && !StringUtils.isEmpty(details.getAccountNumber());
    }

    private boolean isPaymentReferenceNotProvidedForCardPayment(PaymentDetails details) {
        return details.getPaymentMethod() == PaymentMethod.CREDIT_CARD && StringUtils.isEmpty(details.getPaymentReference());
    }

    private boolean isAccountNumberNotProvidedForAccountPayment(PaymentDetails details) {
        return  details.getPaymentMethod() == PaymentMethod.ACCOUNT && StringUtils.isEmpty(details.getAccountNumber());
    }
}
