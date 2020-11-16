package uk.gov.companieshouse.service.dissolution.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.config.FeatureToggleConfig;
import uk.gov.companieshouse.fixtures.PaymentFixtures;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentValidatorTest {

    @InjectMocks
    private PaymentValidator paymentValidator;

    @Mock
    private FeatureToggleConfig featureToggleConfig;

    private PaymentPatchRequest paymentPatchRequest;

    @BeforeEach
    public void setup() {
        paymentPatchRequest = PaymentFixtures.generatePaymentPatchRequest();
    }

    @Test
    public void checkBusinessRules_paymentReferenceAndAccountNumberProvided_returnsValidationMessage() {
        paymentPatchRequest.setPaymentReference("some payment reference");
        paymentPatchRequest.setAccountNumber("some account number");

        when(featureToggleConfig.isPayByAccountEnabled()).thenReturn(true);

        final Optional<String> result = paymentValidator.checkBusinessRules(paymentPatchRequest);

        assertEquals("You cannot submit both a payment reference number and an account number", result.get());
    }

    @Test
    public void checkBusinessRules_paymentReferenceNotProvidedForCardPayment_returnsValidationMessage() {
        paymentPatchRequest.setPaymentReference(null);
        paymentPatchRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(featureToggleConfig.isPayByAccountEnabled()).thenReturn(true);

        final Optional<String> result = paymentValidator.checkBusinessRules(paymentPatchRequest);

        assertEquals("You must provide a payment reference number for a card payment", result.get());
    }

    @Test
    public void checkBusinessRules_accountNumberNotProvidedForAccountPayment_returnsValidationMessage() {
        paymentPatchRequest.setAccountNumber(null);
        paymentPatchRequest.setPaymentMethod(PaymentMethod.ACCOUNT);

        when(featureToggleConfig.isPayByAccountEnabled()).thenReturn(true);

        final Optional<String> result = paymentValidator.checkBusinessRules(paymentPatchRequest);

        assertEquals("You must provide an account number to pay by account", result.get());
    }

    @Test
    public void checkBusinessRules_allRulesSatisfied_returnsEmptyOptional() {
        paymentPatchRequest.setAccountNumber("222222");
        paymentPatchRequest.setPaymentReference(null);
        paymentPatchRequest.setPaymentMethod(PaymentMethod.ACCOUNT);

        when(featureToggleConfig.isPayByAccountEnabled()).thenReturn(true);

        final Optional<String> result = paymentValidator.checkBusinessRules(paymentPatchRequest);

        assertTrue(result.isEmpty());
    }
}
