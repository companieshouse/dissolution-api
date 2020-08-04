package uk.gov.companieshouse.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.officers.OfficersApi;
import uk.gov.companieshouse.config.ApiConfig;
import uk.gov.companieshouse.config.CompanyOfficersConfig;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyOfficersClient {

    private static final UriTemplate GET_OFFICERS_URI = new UriTemplate("/company/{companyNumber}/officers");

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String HEADER_ACCEPT_VALUE = "application/json";
    private static final String HEADER_CONTENT_TYPE_VALUE = "application/json";

    private final CompanyOfficersConfig config;

    @Autowired
    public CompanyOfficersClient(CompanyOfficersConfig config) {
        this.config = config;
    }

    public List<CompanyOfficerApi> getCompanyOfficers(String companyNumber) {
        return Optional
                .ofNullable(
                    WebClient
                        .create(config.getApiUrl())
                        .get()
                        .uri(GET_OFFICERS_URI.expand(companyNumber).toString())
                        .header(HEADER_AUTHORIZATION, config.getApiKey())
                        .header(HEADER_ACCEPT, HEADER_ACCEPT_VALUE)
                        .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                        .retrieve()
                        .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                        .bodyToMono(OfficersApi.class)
                        .block()
                )
                .map(OfficersApi::getItems)
                .orElse(Collections.emptyList());
    }
}
