package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.ApiClientService;
import uk.gov.companieshouse.api.handler.company.CompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.CompanyGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@ExtendWith(MockitoExtension.class)
public class CompanyProfileServiceTest {

    @InjectMocks
    private CompanyProfileService companyProfileService;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private ApiClient apiClient;

    @Mock
    private CompanyResourceHandler companyResourceHandler;

    @Mock
    private CompanyGet companyGet;

    @Mock
    private ApiResponse<CompanyProfileApi> apiResponse;

    @Mock
    private CompanyClosableValidator companyClosableValidator;

    public static final String COMPANY_NUMBER = "12345678";

    @Test
    public void isCompanyClosable_callsCompanyClosableMapper_returnsTrue() {
        final CompanyProfileApi company = CompanyProfileFixtures.generateCompanyProfileApi();

        when(companyClosableValidator.isCompanyClosable(company)).thenReturn(true);

        final boolean isClosable = companyProfileService.isCompanyClosable(company);

        verify(companyClosableValidator).isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void isCompanyClosable_callsCompanyClosableMapper_returnsFalse() {
        final CompanyProfileApi company = CompanyProfileFixtures.generateCompanyProfileApi();

        when(companyClosableValidator.isCompanyClosable(company)).thenReturn(false);

        final boolean isClosable = companyProfileService.isCompanyClosable(company);

        verify(companyClosableValidator).isCompanyClosable(company);

        assertFalse(isClosable);
    }
}
