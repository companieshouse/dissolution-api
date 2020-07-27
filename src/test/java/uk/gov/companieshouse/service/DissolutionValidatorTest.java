package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DissolutionValidatorTest {

    @InjectMocks
    private DissolutionValidator dissolutionValidator;

    @Mock
    private CompanyProfileService companyProfileService;

    @Mock
    private CompanyOfficerService companyOfficerService;

    @Test
    public void checkBusinessRules_allRulesSatisfied_returnsEmptyOptional() {
        final CompanyProfileApi companyProfileApi = CompanyProfileFixtures.generateCompanyProfileApi();

        when(companyProfileService.isCompanyClosable(companyProfileApi)).thenReturn(true);
        when(companyOfficerService.hasEnoughOfficersSelected(companyProfileApi.getCompanyNumber()))
                .thenReturn(true);

        final Optional<String> validationMessage = dissolutionValidator.checkBusinessRules(companyProfileApi);

        assertEquals(Optional.empty(), validationMessage);
    }

    @Test
    public void checkBusinessRules_comapnyNotClosable_returnsValidationMessage() {
        final CompanyProfileApi companyProfileApi = CompanyProfileFixtures.generateCompanyProfileApi();

        when(companyProfileService.isCompanyClosable(companyProfileApi)).thenReturn(false);

        final Optional<String> validationMessage = dissolutionValidator.checkBusinessRules(companyProfileApi);

        assertEquals(Optional.of("Company must be of a closable type and have an active status"), validationMessage);
    }

    @Test
    public void checkBusinessRules_notEnoughDirectorsSelected_returnsValidationMessage() {
        final CompanyProfileApi companyProfileApi = CompanyProfileFixtures.generateCompanyProfileApi();

        when(companyProfileService.isCompanyClosable(companyProfileApi)).thenReturn(true);
        when(companyOfficerService.hasEnoughOfficersSelected(companyProfileApi.getCompanyNumber()))
                .thenReturn(false);

        final Optional<String> validationMessage = dissolutionValidator.checkBusinessRules(companyProfileApi);

        assertEquals(Optional.of("A majority of directors must be selected"), validationMessage);
    }
}
