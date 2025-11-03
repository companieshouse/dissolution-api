package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.util.constant.FeeConstants;

import static org.junit.Assert.assertEquals;

public class RefundRequestMapperTest {

    private final RefundRequestMapper mapper = new RefundRequestMapper();

    @Test
    public void mapToRefundRequest_shouldMapTheAmount() {
        final RefundRequest result = mapper.mapToRefundRequest(FeeConstants.DS01_REFUND_AMOUNT_PENCE);

        assertEquals(FeeConstants.DS01_REFUND_AMOUNT_PENCE, result.getAmount());
    }

}
