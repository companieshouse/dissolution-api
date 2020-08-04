package uk.gov.companieshouse.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.config.CompanyProfileConfig;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;

@Service
public class CompanyProfileClient {

    private static class CompanyNotFoundException extends RuntimeException {}

    private static final UriTemplate GET_COMPANY_URI = new UriTemplate("/company/{companyNumber}");

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String HEADER_ACCEPT_VALUE = "application/json";
    private static final String HEADER_CONTENT_TYPE_VALUE = "application/json";

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
                    .header(HEADER_ACCEPT, HEADER_ACCEPT_VALUE)
                    .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                    .retrieve()
                    .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> { throw new CompanyNotFoundException(); })
                    .bodyToMono(CompanyProfile.class)
                    .block();
        } catch (CompanyNotFoundException ex) {
            return null;
        }
    }
}
