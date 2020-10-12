package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeatureToggleConfig {

    @Value("${featureToggles.uniqueEmails}")
    private boolean uniqueEmailsEnabled;

    public boolean isUniqueEmailsEnabled() {
        return uniqueEmailsEnabled;
    }

    public void setUniqueEmailsEnabled(boolean uniqueEmailsEnabled) {
        this.uniqueEmailsEnabled = uniqueEmailsEnabled;
    }
}
