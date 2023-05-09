package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.PaymentMethod;

@Service
public class PaymentInformationMapper {

    public PaymentInformation mapToPaymentInformation(PaymentPatchRequest paymentPatchRequest) {
        PaymentInformation paymentInformation = new PaymentInformation();

        paymentInformation.setDateTime(paymentPatchRequest.getPaidAt().toLocalDateTime());

        if (paymentPatchRequest.getPaymentMethod() == PaymentMethod.ACCOUNT) {
            paymentInformation.setMethod(PaymentMethod.ACCOUNT);
            paymentInformation.setAccountNumber(paymentPatchRequest.getAccountNumber());
        } else {
            paymentInformation.setMethod(PaymentMethod.CREDIT_CARD);
            paymentInformation.setReference(paymentPatchRequest.getPaymentReference());
        }

        return paymentInformation;
    }

    public PaymentInformation mapPaymentReference(String paymentReference) {
        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setReference(paymentReference);

        return paymentInformation;
    }
}
