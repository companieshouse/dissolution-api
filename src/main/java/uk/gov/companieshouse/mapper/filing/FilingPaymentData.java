package uk.gov.companieshouse.mapper.filing;

import uk.gov.companieshouse.model.domain.PaymentDetails;
import uk.gov.companieshouse.model.enums.PaymentMethod;

public record FilingPaymentData(PaymentMethod method, String reference,
                                String accountNumber) implements PaymentDetails {

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
