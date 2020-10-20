package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    @Value("${featureToggles.refunds}")
    private boolean refundsEnabled;

    public boolean isRefundsEnabled() {
        return refundsEnabled;
    }

    public void setRefundsEnabled(boolean refundsEnabled) {
        this.refundsEnabled = refundsEnabled;
    }
}