package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.model.dto.payment.RefundRequest;

import static org.junit.Assert.assertEquals;

class RefundRequestMapperTest {

    private final RefundRequestMapper mapper = new RefundRequestMapper();

    private static final int DS01_REFUND_AMOUNT_PENCE = 1300;

    @Test
    void mapToRefundRequest_shouldMapTheAmount() {
        final RefundRequest result = mapper.mapToRefundRequest(DS01_REFUND_AMOUNT_PENCE);

        assertEquals(DS01_REFUND_AMOUNT_PENCE, result.getAmount());
    }

}
