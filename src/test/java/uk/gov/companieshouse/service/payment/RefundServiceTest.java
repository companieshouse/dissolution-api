package uk.gov.companieshouse.service.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.PaymentsClient;
import uk.gov.companieshouse.mapper.RefundInformationMapper;
import uk.gov.companieshouse.model.db.payment.RefundInformation;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generateRefundInformation;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generateRefundRequest;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generateRefundResponse;

@ExtendWith(MockitoExtension.class)
public class RefundServiceTest {

    @InjectMocks
    private RefundService refundService;

    @Mock
    private PaymentsClient paymentsClient;

    @Mock
    private RefundInformationMapper refundInformationMapper;

    @Test
    public void refundPayment_callsPaymentsClient() {
        RefundRequest refundRequest = generateRefundRequest();
        String paymentReference = "GYU890";
        RefundResponse refundResponse = generateRefundResponse();
        RefundInformation refund = generateRefundInformation();

        when(paymentsClient.refundPayment(refundRequest, paymentReference)).thenReturn(refundResponse);
        when(refundInformationMapper.mapToRefundInformation(refundResponse)).thenReturn(refund);

        RefundInformation refundResult = refundService.refundPayment(paymentReference, refundRequest);

        verify(paymentsClient).refundPayment(refundRequest, paymentReference);

        assertEquals(refund, refundResult);
    }
}
