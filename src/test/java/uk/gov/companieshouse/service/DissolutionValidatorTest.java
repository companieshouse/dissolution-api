package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class DissolutionValidatorTest {
    private static final String DIRECTOR_NAME = "Jeff";
    private static final String DIRECTOR_EMAIL = "jeff@email.com";

    @InjectMocks
    private DissolutionValidator dissolutionValidator;

    @Mock
    private CompanyProfileService companyProfileService;

    @Mock
    private CompanyOfficerService companyOfficerService;

    @Test
    public void checkBusinessRules_allRulesSatisfied_returnsEmptyOptional() {
        final CompanyProfile companyProfile = CompanyProfileFixtures.generateCompanyProfile();
        final DirectorRequest directorOne = new DirectorRequest();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);
        final List<DirectorRequest> selectedDirectors = Arrays.asList(directorOne);

        when(companyProfileService.isCompanyClosable(companyProfile)).thenReturn(true);
        when(companyOfficerService.hasEnoughOfficersSelected(companyProfile.getCompanyNumber(), selectedDirectors))
                .thenReturn(true);

        final Optional<String> validationMessage = dissolutionValidator.checkBusinessRules(companyProfile, selectedDirectors);

        assertEquals(Optional.empty(), validationMessage);
    }

    @Test
    public void checkBusinessRules_comapnyNotClosable_returnsValidationMessage() {
        final CompanyProfile companyProfile = CompanyProfileFixtures.generateCompanyProfile();
        final DirectorRequest directorOne = new DirectorRequest();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);
        final List<DirectorRequest> selectedDirectors = Arrays.asList(directorOne);

        when(companyProfileService.isCompanyClosable(companyProfile)).thenReturn(false);

        final Optional<String> validationMessage = dissolutionValidator.checkBusinessRules(companyProfile, selectedDirectors);

        assertEquals(Optional.of("Company must be of a closable type and have an active status"), validationMessage);
    }

    @Test
    public void checkBusinessRules_notEnoughDirectorsSelected_returnsValidationMessage() {
        final CompanyProfile companyProfile = CompanyProfileFixtures.generateCompanyProfile();
        final DirectorRequest directorOne = new DirectorRequest();
        directorOne.setName(DIRECTOR_NAME);
        directorOne.setEmail(DIRECTOR_EMAIL);
        final List<DirectorRequest> selectedDirectors = Arrays.asList(directorOne);

        when(companyProfileService.isCompanyClosable(companyProfile)).thenReturn(true);
        when(companyOfficerService.hasEnoughOfficersSelected(companyProfile.getCompanyNumber(), selectedDirectors))
                .thenReturn(false);

        final Optional<String> validationMessage = dissolutionValidator.checkBusinessRules(companyProfile, selectedDirectors);

        assertEquals(Optional.of("A majority of directors must be selected"), validationMessage);
    }
}
