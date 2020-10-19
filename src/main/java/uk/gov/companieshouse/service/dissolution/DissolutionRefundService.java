package uk.gov.companieshouse.service.dissolution;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.RefundInformationMapper;
import uk.gov.companieshouse.mapper.RefundRequestMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.payment.RefundInformation;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;
import uk.gov.companieshouse.service.payment.RefundService;

@Service
public class DissolutionRefundService {
    private static final int REFUND_AMOUNT = 800;

    private final RefundRequestMapper refundRequestMapper;
    private final RefundInformationMapper refundInformationMapper;
    private final RefundService refundService;

    public DissolutionRefundService(
            RefundRequestMapper refundRequestMapper,
            RefundInformationMapper refundInformationMapper,
            RefundService refundService
    ) {
        this.refundRequestMapper = refundRequestMapper;
        this.refundInformationMapper = refundInformationMapper;
        this.refundService = refundService;
    }

    public void handleRefund(Dissolution dissolution) {
        RefundInformation refund = refundService.refundPayment(
            dissolution.getPaymentInformation().getReference(),
            refundRequestMapper.mapToRefundRequest(REFUND_AMOUNT)
        );

        dissolution.getPaymentInformation().setRefund(refund);
    }
}
