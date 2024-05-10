package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;

import static org.junit.Assert.assertEquals;

public class RefundRequestMapperTest {

    private final RefundRequestMapper mapper = new RefundRequestMapper();

    @Test
    public void mapToRefundRequest_shouldMapTheAmount() {
        final RefundRequest result = mapper.mapToRefundRequest(3300);

        assertEquals(3300, result.getAmount());
    }

}
