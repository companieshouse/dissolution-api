package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.client.CompanyProfileClient;
import uk.gov.companieshouse.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.exception.ServiceUnavailableException;
import uk.gov.companieshouse.fixtures.CompanyProfileApiFixtures;
import uk.gov.companieshouse.mapper.CompanyProfileMapper;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.service.dissolution.validator.CompanyClosableValidator;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyProfileTestDataBuilder.aCompany;

@ExtendWith(MockitoExtension.class)
class CompanyProfileServiceTest {

    @InjectMocks
    private CompanyProfileService companyProfileService;

    @Mock
    private CompanyClosableValidator companyClosableValidator;

    @Mock
    private CompanyProfileClient companyProfileClient;

    @Mock
    private CompanyProfileMapper companyProfileMapper;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String PASSTHROUGH_HEADER = "passthrough";

    @Test
    void isCompanyClosable_callsCompanyClosableValidator_returnsTrue() {
        final CompanyProfile company = aCompany().build();

        when(companyClosableValidator.isCompanyClosable(company)).thenReturn(true);

        final boolean isClosable = companyProfileService.isCompanyClosable(company);

        verify(companyClosableValidator).isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    void isCompanyClosable_callsCompanyClosableValidator_returnsFalse() {
        final CompanyProfile company = aCompany().build();

        when(companyClosableValidator.isCompanyClosable(company)).thenReturn(false);

        final boolean isClosable = companyProfileService.isCompanyClosable(company);

        verify(companyClosableValidator).isCompanyClosable(company);

        assertFalse(isClosable);
    }

    @Test
    void getCompanyProfile_returnsCompanyProfileWhenFound() {
        final CompanyProfileApi company = CompanyProfileApiFixtures.generateCompanyProfileApi();
        when(companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(Optional.of(company));
        when(companyProfileMapper.mapToCompanyProfile(company)).thenReturn(aCompany().build());

        final CompanyProfile companyProfile = companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER);

        verify(companyProfileClient, times(1)).getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER);

        assertEquals(company.getCompanyName(), companyProfile.companyName());
        assertEquals(company.getCompanyNumber(), companyProfile.companyNumber());
        assertEquals(company.getCompanyStatus(), companyProfile.companyStatus());
        assertEquals(company.getType(), companyProfile.type());
    }

    @Test
    void getCompanyProfile_throwsNotFound_whenCompanyProfileIsEmpty() {
        when(companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(Optional.empty());

        final var exception = assertThrows(NotFoundException.class,
                () -> companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER));
        assertThat(exception.getMessage(),
                is("Company profile not found for company number " + COMPANY_NUMBER));

        verify(companyProfileClient, times(1)).getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER);
    }

    @Test
    void getCompanyProfile_throwsServiceUnavailable_whenCompanyProfileIsUnavailable() {
        when(companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(ServiceUnavailableException.class);

        assertThrows(ServiceUnavailableException.class,
                () -> companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER));

        verify(companyProfileClient, times(1)).getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER);
    }

    @Test
    void getCompanyProfile_throwsCompanyProfileServiceException_whenCompanyProfileIsUnavailable() {
        when(companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(CompanyProfileServiceException.class);

        assertThrows(CompanyProfileServiceException.class,
                () -> companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER));

        verify(companyProfileClient, times(1)).getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER);
    }
}
