package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.FeatureToggleConfig;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.PaymentMethod;

@Service
public class PaymentInformationMapper {

    private final FeatureToggleConfig featureToggleConfig;

    public PaymentInformationMapper(FeatureToggleConfig featureToggleConfig) {
        this.featureToggleConfig = featureToggleConfig;
    }

    public PaymentInformation mapToPaymentInformation(PaymentPatchRequest paymentPatchRequest) {
        PaymentInformation paymentInformation = new PaymentInformation();

        paymentInformation.setDateTime(paymentPatchRequest.getPaidAt().toLocalDateTime());

        if (featureToggleConfig.isPayByAccountEnabled() && paymentPatchRequest.getPaymentMethod() == PaymentMethod.ACCOUNT) {
            paymentInformation.setMethod(PaymentMethod.ACCOUNT);
            paymentInformation.setAccountNumber(paymentPatchRequest.getAccountNumber());
        } else {
            paymentInformation.setMethod(PaymentMethod.CREDIT_CARD);
            paymentInformation.setReference(paymentPatchRequest.getPaymentReference());
        }

        return paymentInformation;
    }
}
