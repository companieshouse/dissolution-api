package uk.gov.companieshouse.service.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.PaymentsClient;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateRefundResponse;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generateRefundRequest;

@ExtendWith(MockitoExtension.class)
public class RefundServiceTest {

    @InjectMocks
    private RefundService refundService;

    @Mock
    private PaymentsClient paymentsClient;

    @Test
    public void refundPayment_callsPaymentsClient() {
        RefundRequest refundRequest = generateRefundRequest();
        String paymentReference = "GYU890";
        RefundResponse refundResponse = generateRefundResponse();

        when(paymentsClient.refundPayment(refundRequest, paymentReference)).thenReturn(refundResponse);

        refundService.refundPayment(paymentReference, refundRequest);

        verify(paymentsClient).refundPayment(refundRequest, paymentReference);
    }
}
