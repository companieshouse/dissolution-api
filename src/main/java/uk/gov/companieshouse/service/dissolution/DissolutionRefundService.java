package uk.gov.companieshouse.service.dissolution;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.RefundInformationMapper;
import uk.gov.companieshouse.mapper.RefundRequestMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.db.payment.RefundInformation;
import uk.gov.companieshouse.service.payment.RefundService;

@Service
public class DissolutionRefundService {
    private static final int REFUND_AMOUNT = 3300;

    private final RefundRequestMapper refundRequestMapper;
    private final RefundInformationMapper refundInformationMapper;
    private final RefundService refundService;
    private final DissolutionEmailService emailService;
    private final Logger logger;

    public DissolutionRefundService(
            RefundRequestMapper refundRequestMapper,
            RefundInformationMapper refundInformationMapper,
            RefundService refundService,
            DissolutionEmailService emailService,
            Logger logger
    ) {
        this.refundRequestMapper = refundRequestMapper;
        this.refundInformationMapper = refundInformationMapper;
        this.refundService = refundService;
        this.emailService = emailService;
        this.logger = logger;
    }

    public void handleRefund(Dissolution dissolution, DissolutionVerdict verdict) {
        try {
            RefundInformation refund = refundService.refundPayment(
                    dissolution.getPaymentInformation().getReference(),
                    refundRequestMapper.mapToRefundRequest(REFUND_AMOUNT)
            );

            dissolution.getPaymentInformation().setRefund(refund);
        } catch (WebClientException e) {
            logger.error("Automatic refund failed, sending rejection email to finance", e);

            emailService.sendRejectionEmailToFinance(dissolution, verdict);
        }
    }
}
