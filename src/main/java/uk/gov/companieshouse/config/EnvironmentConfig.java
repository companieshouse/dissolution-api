package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentConfig {

    @Value("${env.name}")
    private String environmentName;

    @Value("${env.chsUrl}")
    private String chsUrl;

    @Value("${email.chsFinanceEmail}")
    private String chsFinanceEmail;
    
    @Value("${cdn.host}")
    private String cdnHost;

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getChsUrl() {
        return chsUrl;
    }

    public void setChsUrl(String chsUrl) {
        this.chsUrl = chsUrl;
    }

    public String getCdnHost() {
        return cdnHost;
    }

    public void setCdnHost(String cdnHost) {
        this.cdnHost = cdnHost;
    }

    public String getChsFinanceEmail() {
        return chsFinanceEmail;
    }

    public void setChsFinanceEmail(String chsFinanceEmail) {
        this.chsFinanceEmail = chsFinanceEmail;
    }
}
