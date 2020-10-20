package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.mapper.RefundRequestMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.payment.RefundInformation;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.service.payment.RefundService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.*;

@ExtendWith(MockitoExtension.class)
public class DissolutionRefundServiceTest {

    @InjectMocks
    private DissolutionRefundService dissolutionRefundService;

    @Mock
    private RefundService refundService;

    @Mock
    private RefundRequestMapper refundRequestMapper;

    private static final int REFUND_AMOUNT = 800;

    @Test
    public void handleRefund_refundPaidDissolution() {
        final Dissolution dissolution = generateDissolution();
        final RefundRequest refundRequest = generateRefundRequest();
        refundRequest.setAmount(REFUND_AMOUNT);
        final String paymentReference = dissolution.getPaymentInformation().getReference();
        final RefundInformation refundInformation = generateRefundInformation();

        when(refundRequestMapper.mapToRefundRequest(REFUND_AMOUNT)).thenReturn(refundRequest);
        when(refundService.refundPayment(paymentReference, refundRequest)).thenReturn(refundInformation);

        dissolutionRefundService.handleRefund(dissolution);

        assertEquals(dissolution.getPaymentInformation().getRefund(), refundInformation);
    }
}
