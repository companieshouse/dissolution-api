package uk.gov.companieshouse.service.payment;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.PaymentsClient;
import uk.gov.companieshouse.mapper.RefundInformationMapper;
import uk.gov.companieshouse.model.db.payment.RefundInformation;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;

@Service
public class RefundService {
    private final PaymentsClient paymentsClient;
    private final RefundInformationMapper refundInformationMapper;

    public RefundService(
        PaymentsClient paymentsClient,
        RefundInformationMapper refundInformationMapper
    ) {
        this.paymentsClient = paymentsClient;
        this.refundInformationMapper = refundInformationMapper;
    }

    public RefundInformation refundPayment(String paymentReference, RefundRequest refundRequest) {
        RefundResponse refundResponse = paymentsClient.refundPayment(
                refundRequest,
                paymentReference
        );

        return refundInformationMapper.mapToRefundInformation(refundResponse);
    }
}
