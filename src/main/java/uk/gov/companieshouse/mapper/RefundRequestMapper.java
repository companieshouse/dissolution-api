package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;

@Service
public class RefundRequestMapper {
    public RefundRequest mapToRefundRequest(int amount) {
        RefundRequest refundRequest = new RefundRequest(amount);

        return refundRequest;
    }
}
