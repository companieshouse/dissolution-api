package uk.gov.companieshouse.service.dissolution.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.service.CompanyOfficerService;
import uk.gov.companieshouse.service.CompanyProfileService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorRequest;

@ExtendWith(MockitoExtension.class)
public class DissolutionValidatorTest {

    @InjectMocks
    private DissolutionValidator dissolutionValidator;

    @Mock
    private CompanyProfileService companyProfileService;

    @Mock
    private CompanyOfficerService companyOfficerService;

    private final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
    private final Map<String, CompanyOfficer> companyDirectors = Map.of("abc123", generateCompanyOfficer());
    private final List<DirectorRequest> selectedDirectors = Collections.singletonList(generateDirectorRequest());

    @Test
    public void checkBusinessRules_allRulesSatisfied_returnsEmptyOptional() {
        when(companyProfileService.isCompanyClosable(company)).thenReturn(true);
        when(companyOfficerService.areSelectedDirectorsValid(companyDirectors, selectedDirectors)).thenReturn(Optional.empty());

        final Optional<String> result = dissolutionValidator.checkBusinessRules(company, companyDirectors, selectedDirectors);

        assertTrue(result.isEmpty());
    }

    @Test
    public void checkBusinessRules_companyNotClosable_returnsValidationMessage() {
        when(companyProfileService.isCompanyClosable(company)).thenReturn(false);

        final Optional<String> result = dissolutionValidator.checkBusinessRules(company, companyDirectors, selectedDirectors);

        assertEquals("Company must be of a closable type, have an active status and must not be an overseas company", result.get());
    }

    @Test
    public void checkBusinessRules_invalidSelectedDirectors_returnsValidationMessage() {
        when(companyProfileService.isCompanyClosable(company)).thenReturn(true);
        when(companyOfficerService.areSelectedDirectorsValid(companyDirectors, selectedDirectors)).thenReturn(Optional.of("Some directors error"));

        final Optional<String> result = dissolutionValidator.checkBusinessRules(company, companyDirectors, selectedDirectors);

        assertEquals("Some directors error", result.get());
    }
}
