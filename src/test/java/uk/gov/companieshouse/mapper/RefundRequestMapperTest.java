package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.config.constant.FeeConstants;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;

import static org.junit.Assert.assertEquals;

class RefundRequestMapperTest {

    private final RefundRequestMapper mapper = new RefundRequestMapper();

    @Test
    void mapToRefundRequest_shouldMapTheAmount() {
        final RefundRequest result = mapper.mapToRefundRequest(FeeConstants.DS01_REFUND_AMOUNT_PENCE);

        assertEquals(FeeConstants.DS01_REFUND_AMOUNT_PENCE, result.getAmount());
    }

}
