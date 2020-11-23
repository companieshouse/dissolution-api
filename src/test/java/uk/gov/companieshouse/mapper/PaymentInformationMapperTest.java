package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.config.FeatureToggleConfig;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentPatchRequest;

@ExtendWith(MockitoExtension.class)
public class PaymentInformationMapperTest {
    private static final String ACCOUNT_NUMBER = "222222";
    private static final String PAYMENT_REFERENCE = "TEST_REFERENCE";

    @InjectMocks
    private PaymentInformationMapper paymentInformationMapper;

    @Mock
    private FeatureToggleConfig featureToggleConfig;

    @Test
    public void mapToPaymentInformation_getsPaymentInformation_payByAccountFeatureToggleOn() {
        final PaymentPatchRequest paymentPatchRequest = generatePaymentPatchRequest();
        paymentPatchRequest.setPaymentReference(null);
        paymentPatchRequest.setPaymentMethod(PaymentMethod.ACCOUNT);
        paymentPatchRequest.setAccountNumber(ACCOUNT_NUMBER);

        when(featureToggleConfig.isPayByAccountEnabled()).thenReturn(true);

        final PaymentInformation paymentInformation = paymentInformationMapper.mapToPaymentInformation(paymentPatchRequest);

        assertNotNull(paymentInformation.getDateTime());
        assertEquals(PaymentMethod.ACCOUNT, paymentInformation.getMethod());
        assertEquals(ACCOUNT_NUMBER, paymentInformation.getAccountNumber());
        assertNull(paymentInformation.getReference());
    }

    @Test
    public void mapToPaymentInformation_getsPaymentInformation_payByAccountFeatureToggleOff() {
        final PaymentPatchRequest paymentPatchRequest = generatePaymentPatchRequest();
        paymentPatchRequest.setPaymentReference(PAYMENT_REFERENCE);
        paymentPatchRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        paymentPatchRequest.setAccountNumber(null);

        when(featureToggleConfig.isPayByAccountEnabled()).thenReturn(false);

        final PaymentInformation paymentInformation = paymentInformationMapper.mapToPaymentInformation(paymentPatchRequest);

        assertNotNull(paymentInformation.getDateTime());
        assertEquals(PaymentMethod.CREDIT_CARD, paymentInformation.getMethod());
        assertEquals(PAYMENT_REFERENCE, paymentInformation.getReference());
        assertNull(paymentInformation.getAccountNumber());
    }
}
