package uk.gov.companieshouse.config;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = FeeConfig.class)
@ActiveProfiles("test")
class FeeConfigTest {

    private static final int DSO1_REFUND_AMOUNT_PENC = 1300;

    private static final String PAYMENT_AMOUNT = "13";

    @Autowired
    FeeConfig feeConfig;

    @Test
    void testFeeConfigValueForDS01Refund() {
        assertEquals(DSO1_REFUND_AMOUNT_PENC, feeConfig.getDS01RefundAmountPence());
    }

    @Test
    void testFeeConfigValueForPaymentAmount() {
        assertEquals(PAYMENT_AMOUNT, feeConfig.getPaymentAmount());
    }

}
