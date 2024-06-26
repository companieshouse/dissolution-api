package uk.gov.companieshouse.client;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.CompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.CompanyGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.exception.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.service.dissolution.validator.CompanyClosableValidator;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyProfileClientImplTest {

    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String URI = "/company/" + COMPANY_NUMBER;

    @Mock
    private ApiClientService apiClientService;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private CompanyGet companyGet;
    @Mock
    private CompanyResourceHandler companyResourceHandler;
    @Mock
    private ApiResponse<CompanyProfileApi> apiResponse;
    @Mock
    private Logger logger;
    @Mock
    private CompanyProfileApi mockCompanyProfileApi;
    @Mock
    private CompanyClosableValidator validator;
    private CompanyProfileClientImpl companyProfileClient;

    @BeforeEach
    void setUp() {
        companyProfileClient = new CompanyProfileClientImpl(apiClientService, logger, validator);
    }

    @Test
    void companyProfileIsReturnedFromAPIWhenFound() throws IOException, URIValidationException {
        when(apiResponse.getData()).thenReturn(mockCompanyProfileApi);
        when(companyGet.execute()).thenReturn(apiResponse);
        when(companyResourceHandler.get(URI)).thenReturn(companyGet);
        when(internalApiClient.company()).thenReturn(companyResourceHandler);
        when(apiClientService.getInternalApiClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

        CompanyProfileApi companyProfile = companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER);

        assertEquals(companyProfile, mockCompanyProfileApi);
    }

    @Test
    void exceptionIsThrownWhenCompanyProfileIsNotFoundUriException() throws IOException, URIValidationException {
        when(companyGet.execute()).thenThrow(URIValidationException.class);
        when(companyResourceHandler.get(URI)).thenReturn(companyGet);
        when(internalApiClient.company()).thenReturn(companyResourceHandler);
        when(apiClientService.getInternalApiClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

        final var exception = assertThrows(CompanyProfileServiceException.class,
                () -> companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER));
        assertThat(exception.getMessage(),
                is("Error Retrieving company profile " + COMPANY_NUMBER));
    }

    @Test
    void exceptionIsThrownWhenCompanyProfileIsNotFoundHttpResponse() throws IOException, URIValidationException {
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(new HttpResponseException.Builder(404, "404 Not Found", new HttpHeaders()));
        when(companyGet.execute()).thenThrow(apiErrorResponseException);
        when(companyResourceHandler.get(URI)).thenReturn(companyGet);
        when(internalApiClient.company()).thenReturn(companyResourceHandler);
        when(apiClientService.getInternalApiClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

        final var exception = assertThrows(CompanyProfileServiceException.class,
                () -> companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER));
        assertThat(exception.getMessage(),
                is("Error Retrieving company profile " + COMPANY_NUMBER));
    }

    @Test
    void exceptionIsThrownWhenCompanyProfileServiceUnavailable() throws IOException, URIValidationException {
        when(companyGet.execute()).thenThrow(ApiErrorResponseException.class);
        when(companyResourceHandler.get(URI)).thenReturn(companyGet);
        when(internalApiClient.company()).thenReturn(companyResourceHandler);
        when(apiClientService.getInternalApiClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

        final var exception = assertThrows(ServiceUnavailableException.class,
                () -> companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER));
        assertThat(exception.getMessage(),
                is("The service is down. Try again later"));
    }
}
