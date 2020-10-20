package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.payment.RefundInformation;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;

@Service
public class RefundInformationMapper {
    public RefundInformation mapToRefundInformation(RefundResponse refundResponse) {
        RefundInformation refundInformation = new RefundInformation();
        
        refundInformation.setRefundId(refundResponse.getRefundId());
        refundInformation.setCreatedDateTime(refundResponse.getCreatedDateTime());
        refundInformation.setAmount(refundResponse.getAmount());

        return refundInformation;
    }
}
