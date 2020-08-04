package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CompanyProfileConfig extends ApiConfig {

    @Value("${companyProfile.host}")
    private String companyProfileHost;

    public String getCompanyProfileHost() {
        return companyProfileHost;
    }

    public void setCompanyProfileHost(String companyProfileHost) {
        this.companyProfileHost = companyProfileHost;
    }
}
