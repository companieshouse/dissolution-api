package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChipsConfig {

    @Value("${chips.host}")
    private String chipsHost;

    @Value("${chips.retryLimit}")
    private int chipsRetryLimit;

    public String getChipsHost() {
        return chipsHost;
    }

    public void setChipsHost(String chipsHost) {
        this.chipsHost = chipsHost;
    }

    public int getChipsRetryLimit() {
        return chipsRetryLimit;
    }

    public void setChipsRetryLimit(int chipsRetryLimit) {
        this.chipsRetryLimit = chipsRetryLimit;
    }
}
