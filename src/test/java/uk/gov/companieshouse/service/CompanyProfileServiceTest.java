package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.service.dissolution.validator.CompanyClosableValidator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@ExtendWith(MockitoExtension.class)
public class CompanyProfileServiceTest {

    @InjectMocks
    private CompanyProfileService companyProfileService;

    @Mock
    private CompanyClosableValidator companyClosableValidator;

    public static final String COMPANY_NUMBER = "12345678";

    @Test
    public void isCompanyClosable_callsCompanyClosableMapper_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();

        when(companyClosableValidator.isCompanyClosable(company)).thenReturn(true);

        final boolean isClosable = companyProfileService.isCompanyClosable(company);

        verify(companyClosableValidator).isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void isCompanyClosable_callsCompanyClosableMapper_returnsFalse() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();

        when(companyClosableValidator.isCompanyClosable(company)).thenReturn(false);

        final boolean isClosable = companyProfileService.isCompanyClosable(company);

        verify(companyClosableValidator).isCompanyClosable(company);

        assertFalse(isClosable);
    }
}
