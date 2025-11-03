package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import uk.gov.companieshouse.config.constant.FeeConstants;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.RefundRequestMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.db.payment.RefundInformation;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.service.payment.RefundService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionVerdict;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generateRefundInformation;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generateRefundRequest;

@ExtendWith(MockitoExtension.class)
class DissolutionRefundServiceTest {

    @InjectMocks
    private DissolutionRefundService dissolutionRefundService;

    @Mock
    private RefundService refundService;

    @Mock
    private RefundRequestMapper refundRequestMapper;

    @Mock
    private DissolutionEmailService emailService;

    @Mock
    private Logger logger;

    private static final int REFUND_AMOUNT = FeeConstants.DS01_REFUND_AMOUNT_PENCE;

    @Test
    void handleRefund_refundPaidDissolution() {
        final Dissolution dissolution = generateDissolution();
        final DissolutionVerdict dissolutionVerdict = generateDissolutionVerdict();
        final RefundRequest refundRequest = generateRefundRequest();
        refundRequest.setAmount(REFUND_AMOUNT);
        final String paymentReference = dissolution.getPaymentInformation().getReference();
        final RefundInformation refundInformation = generateRefundInformation();

        when(refundRequestMapper.mapToRefundRequest(REFUND_AMOUNT)).thenReturn(refundRequest);
        when(refundService.refundPayment(paymentReference, refundRequest)).thenReturn(refundInformation);

        dissolutionRefundService.handleRefund(dissolution, dissolutionVerdict);

        assertEquals(dissolution.getPaymentInformation().getRefund(), refundInformation);
    }

    @Test
    void handleRefund_refundPaidDissolution_throwException_sendEmailToFinance() {
        final Dissolution dissolution = generateDissolution();
        final DissolutionVerdict dissolutionVerdict = generateDissolutionVerdict();
        final RefundRequest refundRequest = generateRefundRequest();
        refundRequest.setAmount(REFUND_AMOUNT);
        final String paymentReference = dissolution.getPaymentInformation().getReference();

        when(refundRequestMapper.mapToRefundRequest(REFUND_AMOUNT)).thenReturn(refundRequest);
        when(refundService.refundPayment(paymentReference, refundRequest))
                .thenThrow(new WebClientResponseException(400, "Bad Request", null, null, null));

        dissolutionRefundService.handleRefund(dissolution, dissolutionVerdict);

        verify(emailService).sendRejectionEmailToFinance(dissolution, dissolutionVerdict);
    }
}
