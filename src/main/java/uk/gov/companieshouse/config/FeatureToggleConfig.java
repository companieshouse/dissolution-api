package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    @Value("${featureToggles.payByAccount}")
    private boolean payByAccountEnabled;

    public boolean isPayByAccountEnabled() {
        return payByAccountEnabled;
    }

    public void setPayByAccountEnabled(boolean payByAccountEnabled) {
        this.payByAccountEnabled = payByAccountEnabled;
    }
}