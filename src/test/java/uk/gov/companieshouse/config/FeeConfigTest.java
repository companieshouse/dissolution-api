package uk.gov.companieshouse.config;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FeeConfig.class)
@ActiveProfiles("test")
class FeeConfigTest {

    private static final int REFUND_AMOUNT_PENCE = 1300;

    private static final String PAYMENT_AMOUNT = "13";

    @Autowired
    FeeConfig feeConfig;

    @Test
    void testFeeConfigValueForRefundAmountPence() {
        assertEquals(REFUND_AMOUNT_PENCE, feeConfig.getRefundAmountPence());
    }

    @Test
    void testFeeConfigValueForClosingCostPounds() {
        assertEquals(PAYMENT_AMOUNT, feeConfig.getClosingPounds());
    }

}
