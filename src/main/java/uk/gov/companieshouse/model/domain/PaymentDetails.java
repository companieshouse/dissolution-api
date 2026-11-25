package uk.gov.companieshouse.model.domain;

import uk.gov.companieshouse.model.enums.PaymentMethod;

public interface PaymentDetails {
    PaymentMethod getPaymentMethod();
    String getPaymentReference();
    String getAccountNumber();
}
