package uk.gov.companieshouse.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.config.CompanyProfileConfig;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;

import static uk.gov.companieshouse.model.Constants.CONTENT_TYPE_JSON;
import static uk.gov.companieshouse.model.Constants.HEADER_ACCEPT;
import static uk.gov.companieshouse.model.Constants.HEADER_AUTHORIZATION;
import static uk.gov.companieshouse.model.Constants.HEADER_CONTENT_TYPE;

@Service
public class CompanyProfileClient {

    private static class CompanyNotFoundException extends RuntimeException {}

    private static final UriTemplate GET_COMPANY_URI = new UriTemplate("/company/{companyNumber}");

    private final CompanyProfileConfig companyProfileConfig;

    @Autowired
    public CompanyProfileClient(CompanyProfileConfig companyProfileConfig) {
        this.companyProfileConfig = companyProfileConfig;
    }

    public CompanyProfile getCompanyProfile(String companyNumber) {
        try {
            return WebClient
                    .create(companyProfileConfig.getCompanyProfileHost())
                    .get()
                    .uri(GET_COMPANY_URI.expand(companyNumber).toString())
                    .header(HEADER_AUTHORIZATION, companyProfileConfig.getApiKey())
                    .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .retrieve()
                    .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> { throw new CompanyNotFoundException(); })
                    .bodyToMono(CompanyProfile.class)
                    .block();
        } catch (CompanyNotFoundException ex) {
            return null;
        }
    }
}
