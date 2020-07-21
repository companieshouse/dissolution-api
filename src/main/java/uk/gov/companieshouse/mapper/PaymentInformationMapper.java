package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import java.sql.Timestamp;

@Service
public class PaymentInformationMapper {
    public PaymentInformation mapToPaymentInformation(PaymentMethod paymentMethod, String paymentReference, Timestamp paidAt) {
        PaymentInformation paymentInformation = new PaymentInformation();

        paymentInformation.setDateTime(paidAt.toLocalDateTime());
        paymentInformation.setMethod(paymentMethod);
        paymentInformation.setReference(paymentReference);

        return paymentInformation;
    }
}
