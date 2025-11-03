package uk.gov.companieshouse.config;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FeeConfig.class)
@ActiveProfiles("test")
class FeeConfigTest {

    private static final int DSO1_REFUND_AMOUNT_PIECE = 1300;

    @Autowired
    FeeConfig feeConfig;

    @Test
    void testFeeConfigValue() {
        assertEquals(DSO1_REFUND_AMOUNT_PIECE, feeConfig.getDS01RefundAmountPiece());
    }

}
