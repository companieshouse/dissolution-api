package uk.gov.companieshouse.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.exception.ServiceException;
import java.util.List;

@Component
public class CompanyProfileClient {
    private static final UriTemplate GET_COMPANY_URI = new UriTemplate("/company/{companyNumber}");

    private final ApiClientService apiClientService;

    @Autowired
    public CompanyProfileClient(
            ApiClientService apiClientService
    ) {
        this.apiClientService = apiClientService;
    }

    public CompanyProfileApi getCompanyProfile(String companyNumber) {
        ApiClient apiClient = apiClientService.getPublicApiClient();
        ApiResponse<CompanyProfileApi> apiResponse;
        CompanyProfileApi companyProfileApi;

        try {
            String uri = GET_COMPANY_URI.expand(companyNumber).toString();
            apiResponse = apiClient.company().get(uri).execute();
            if (apiResponse.getStatusCode() == 404) {
                return null;
            }
            companyProfileApi = apiResponse.getData();
        } catch (ApiErrorResponseException ex) {
            throw new RuntimeException("Error retrieving Company Details", ex);
        } catch (URIValidationException ex) {
            throw new RuntimeException("Invalid URI for Company Details", ex);
        }

        return companyProfileApi;
    }
}
