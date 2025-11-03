package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeeConfig {
    
    /* DS01 Refund Amount */
    @Value("${fee.ds01RefundAmountPiece}")
    private int ds01RefundAmountPiece;

    public int getDS01RefundAmountPiece() {
        return ds01RefundAmountPiece;
    }

}
