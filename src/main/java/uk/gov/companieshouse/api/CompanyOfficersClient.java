package uk.gov.companieshouse.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.exception.ServiceException;
import java.util.List;

@Component
public class CompanyOfficersClient {
    private static final UriTemplate GET_OFFICERS_URI = new UriTemplate("/company/{companyNumber}/officers");

    private final ApiClientService apiClientService;

    @Autowired
    public CompanyOfficersClient(
            ApiClientService apiClientService
    ) {
        this.apiClientService = apiClientService;
    }

    public List<CompanyOfficerApi> getCompanyOfficers(String companyNumber) {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        List<CompanyOfficerApi> companyOfficerApi;

        try {
            String uri = GET_OFFICERS_URI.expand(companyNumber).toString();
            companyOfficerApi = apiClient.officers().list(uri).execute().getData().getItems();
        } catch (ApiErrorResponseException ex) {
            throw new RuntimeException("Error retrieving Company Officers", ex);
        } catch (URIValidationException ex) {
            throw new RuntimeException("Invalid URI for Company Officers", ex);
        }

        return companyOfficerApi;
    }
}
