package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.mapper.RefundInformationMapper;
import uk.gov.companieshouse.mapper.RefundRequestMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.payment.RefundInformation;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.payment.RefundService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;

@ExtendWith(MockitoExtension.class)
public class DissolutionRefundServiceTest {

    @InjectMocks
    private DissolutionRefundService dissolutionRefundService;

    @Mock
    private RefundService refundService;

    @Mock
    private RefundRequestMapper refundRequestMapper;

    @Mock
    private RefundInformationMapper refundInformationMapper;

    @Mock
    private DissolutionRepository repository;

    private static final int REFUND_AMOUNT = 800;

    @Test
    public void handleRefund_refundPaidDissolution() {
        final Dissolution dissolution = generateDissolution();
        final RefundRequest refundRequest = new RefundRequest(REFUND_AMOUNT);
        final RefundResponse refundResponse = generateRefundResponse();
        final String paymentReference = dissolution.getPaymentInformation().getReference();
        final RefundInformation refundInformation = generateRefundInformation();

        when(refundRequestMapper.mapToRefundRequest(REFUND_AMOUNT)).thenReturn(refundRequest);
        when(refundService.refundPayment(paymentReference, refundRequest)).thenReturn(refundResponse);
        when(refundInformationMapper.mapToRefundInformation(refundResponse)).thenReturn(refundInformation);

        dissolutionRefundService.handleRefund(dissolution);

        verify(repository).save(dissolution);

        assertEquals(dissolution.getPaymentInformation().getRefund().getAmount(), REFUND_AMOUNT);
        assertEquals(dissolution.getPaymentInformation().getRefund().getRefundId(), refundInformation.getRefundId());
    }
}
