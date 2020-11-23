package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    @Value("${featureToggles.payByAccount}")
    private boolean payByAccountEnabled;

    @Value("${featureToggles.refunds}")
    private boolean refundsEnabled;

    public boolean isPayByAccountEnabled() {
        return payByAccountEnabled;
    }

    public void setPayByAccountEnabled(boolean payByAccountEnabled) {
        this.payByAccountEnabled = payByAccountEnabled;
    }

    public boolean isRefundsEnabled() {
        return refundsEnabled;
    }

    public void setRefundsEnabled(boolean refundsEnabled) {
        this.refundsEnabled = refundsEnabled;
    }
}