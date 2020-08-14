package uk.gov.companieshouse.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriTemplate;
import reactor.core.publisher.Mono;
import uk.gov.companieshouse.config.CompanyOfficersConfig;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficersResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.companieshouse.model.Constants.CONTENT_TYPE_JSON;
import static uk.gov.companieshouse.model.Constants.HEADER_ACCEPT;
import static uk.gov.companieshouse.model.Constants.HEADER_AUTHORIZATION;
import static uk.gov.companieshouse.model.Constants.HEADER_CONTENT_TYPE;

@Service
public class CompanyOfficersClient {

    private static final UriTemplate GET_OFFICERS_URI = new UriTemplate("/company/{companyNumber}/officers");

    private final CompanyOfficersConfig config;

    @Autowired
    public CompanyOfficersClient(CompanyOfficersConfig config) {
        this.config = config;
    }

    public List<CompanyOfficer> getCompanyOfficers(String companyNumber) {
        return Optional
                .ofNullable(
                    WebClient
                        .create(config.getApiUrl())
                        .get()
                        .uri(GET_OFFICERS_URI.expand(companyNumber).toString())
                        .header(HEADER_AUTHORIZATION, config.getApiKey())
                        .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                        .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                        .retrieve()
                        .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> Mono.empty())
                        .bodyToMono(CompanyOfficersResponse.class)
                        .block()
                )
                .map(CompanyOfficersResponse::getItems)
                .orElse(Collections.emptyList());
    }
}
