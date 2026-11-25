package uk.gov.companieshouse.mapper.filing;

import uk.gov.companieshouse.model.domain.PaymentDetails;
import uk.gov.companieshouse.model.enums.PaymentMethod;

/**
 * FilingPaymentData is an immutable, short-lived value that is used to encapsulate the payment data associated with a filing.
 * Validated by {@link uk.gov.companieshouse.service.dissolution.validator.PaymentValidator} by implementing the {@link PaymentDetails} interface.
 */
public record FilingPaymentData(PaymentMethod method, String reference,
                                String accountNumber) implements PaymentDetails {

    public static FilingPaymentData account(String accountNumber) {
        return new FilingPaymentData(PaymentMethod.ACCOUNT, null, accountNumber);
    }

    public static FilingPaymentData card(String reference) {
        return new FilingPaymentData(PaymentMethod.CREDIT_CARD, reference, null);
    }

    @Override
    public PaymentMethod getPaymentMethod() {
        return method;
    }

    @Override
    public String getPaymentReference() {
        return reference;
    }

    @Override
    public String getAccountNumber() {
        return accountNumber;
    }
}
