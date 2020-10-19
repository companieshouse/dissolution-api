package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    @Value("${featureToggles.automaticallyRequestRefund}")
    private boolean automaticallyRequestRefundEnabled;

    public boolean isAutomaticallyRequestRefundEnabled() {
        return automaticallyRequestRefundEnabled;
    }

    public void setAutomaticallyRequestRefundEnabled(boolean automaticallyRequestRefundEnabled) {
        this.automaticallyRequestRefundEnabled = automaticallyRequestRefundEnabled;
    }
}