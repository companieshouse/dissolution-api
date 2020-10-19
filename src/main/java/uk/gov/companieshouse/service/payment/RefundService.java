package uk.gov.companieshouse.service.payment;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.PaymentsClient;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;

import java.util.Optional;

@Service
public class RefundService {
    private final PaymentsClient paymentsClient;

    public RefundService(
        PaymentsClient paymentsClient
    ) {
        this.paymentsClient = paymentsClient;
    }

    public Optional<RefundResponse> refundPayment(String paymentReference, RefundRequest refundRequest) {
        return paymentsClient.refundPayment(
                refundRequest,
                paymentReference
        );
    }
}
