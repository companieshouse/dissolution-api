package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeeConfig {
    
    /* DS01 Refund Amount */
    @Value("${fee.ds01RefundAmountPence}")
    private int ds01RefundAmountPence;

    public int getDS01RefundAmountPence() {
        return ds01RefundAmountPence;
    }

}
