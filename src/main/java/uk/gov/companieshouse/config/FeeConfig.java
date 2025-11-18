package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeeConfig {
    
    @Value("${fee.llds01AndDs01RefundAmountPence}")
    private int refundAmountPence;

    @Value("${fee.llds01AndDs01ClosingCostPounds}")
    private String closingPounds;

    public int getRefundAmountPence() {
        return refundAmountPence;
    }

    public String getClosingPounds() {
        return closingPounds;
    }
}
