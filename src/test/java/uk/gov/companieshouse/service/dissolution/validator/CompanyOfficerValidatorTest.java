package uk.gov.companieshouse.service.dissolution.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.config.FeatureToggleConfig;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorRequest;

@ExtendWith(MockitoExtension.class)
public class CompanyOfficerValidatorTest {

    private static final String OFFICER_ID_ONE = "abc123";
    private static final String OFFICER_ID_TWO = "def456";
    private static final String OFFICER_ID_THREE = "ghi789";

    private static final String DIRECTOR_EMAIL_ONE = "directorOne@mail.com";
    private static final String DIRECTOR_EMAIL_TWO = "directorTwo@mail.com";
    private static final String DIRECTOR_EMAIL_TWO_LOWER = "directortwo@mail.com";

    @InjectMocks
    private CompanyOfficerValidator validator;

    @Mock
    private FeatureToggleConfig featureToggleConfig;

    @Test
    public void areSelectedDirectorsValid_duplicateOfficerIdsProvided_returnsAnError() {
        final DirectorRequest selectedDirector1 = generateDirectorRequest();
        selectedDirector1.setOfficerId(OFFICER_ID_ONE);

        final DirectorRequest selectedDirector2 = generateDirectorRequest();
        selectedDirector2.setOfficerId(OFFICER_ID_ONE);

        final List<DirectorRequest> selectedDirectors = Arrays.asList(selectedDirector1, selectedDirector2);

        final Optional<String> result = validator.areSelectedDirectorsValid(new HashMap<>(), selectedDirectors);

        assertEquals("Officer IDs must be unique", result.get());
    }

    @Test
    public void areSelectedDirectorsValid_duplicateDirectorEmailsProvided_returnsAnError_whenUniqueEmailsEnabled() {
        final DirectorRequest selectedDirector1 = generateDirectorRequest();
        selectedDirector1.setOfficerId(OFFICER_ID_ONE);
        selectedDirector1.setEmail(DIRECTOR_EMAIL_ONE);

        final DirectorRequest selectedDirector2 = generateDirectorRequest();
        selectedDirector2.setOfficerId(OFFICER_ID_TWO);
        selectedDirector2.setEmail(DIRECTOR_EMAIL_ONE);

        final List<DirectorRequest> selectedDirectors = Arrays.asList(selectedDirector1, selectedDirector2);

        when(featureToggleConfig.isUniqueEmailsEnabled()).thenReturn(true);

        final Optional<String> result = validator.areSelectedDirectorsValid(new HashMap<>(), selectedDirectors);

        assertEquals("Director emails must be unique", result.get());
    }

    @Test
    public void areSelectedDirectorsValid_duplicateDirectorCaseInsensitiveEmailsProvided_returnsAnError_whenUniqueEmailsEnabled() {
        final DirectorRequest selectedDirector1 = generateDirectorRequest();
        selectedDirector1.setOfficerId(OFFICER_ID_ONE);
        selectedDirector1.setEmail(DIRECTOR_EMAIL_TWO);

        final DirectorRequest selectedDirector2 = generateDirectorRequest();
        selectedDirector2.setOfficerId(OFFICER_ID_TWO);
        selectedDirector2.setEmail(DIRECTOR_EMAIL_TWO_LOWER);

        final List<DirectorRequest> selectedDirectors = Arrays.asList(selectedDirector1, selectedDirector2);

        when(featureToggleConfig.isUniqueEmailsEnabled()).thenReturn(true);

        final Optional<String> result = validator.areSelectedDirectorsValid(new HashMap<>(), selectedDirectors);

        assertEquals("Director emails must be unique", result.get());
    }

    @Test
    public void areSelectedDirectorsValid_duplicateDirectorCaseInsensitiveEmailsProvided_returnsNoError_whenUniqueEmailsDisabled() {
        final DirectorRequest selectedDirector1 = generateDirectorRequest();
        selectedDirector1.setOfficerId(OFFICER_ID_ONE);
        selectedDirector1.setEmail(DIRECTOR_EMAIL_ONE);

        final DirectorRequest selectedDirector2 = generateDirectorRequest();
        selectedDirector2.setOfficerId(OFFICER_ID_TWO);
        selectedDirector2.setEmail(DIRECTOR_EMAIL_ONE);

        final Map<String, CompanyOfficer> companyDirectors = Map.of(
                OFFICER_ID_ONE, generateCompanyOfficer(),
                OFFICER_ID_TWO, generateCompanyOfficer(),
                OFFICER_ID_THREE, generateCompanyOfficer()
        );

        final List<DirectorRequest> selectedDirectors = Arrays.asList(selectedDirector1, selectedDirector2);

        when(featureToggleConfig.isUniqueEmailsEnabled()).thenReturn(false);

        final Optional<String> result = validator.areSelectedDirectorsValid(companyDirectors, selectedDirectors);

        assertTrue(result.isEmpty());
    }

    @Test
    public void areSelectedDirectorsValid_officerIdDoesNotExist_returnsAnError() {
        final DirectorRequest selectedDirector = generateDirectorRequest();
        selectedDirector.setOfficerId(OFFICER_ID_ONE);

        final Map<String, CompanyOfficer> companyDirectors = Map.of(OFFICER_ID_TWO, generateCompanyOfficer());

        final List<DirectorRequest> selectedDirectors = Collections.singletonList(selectedDirector);

        final Optional<String> result = validator.areSelectedDirectorsValid(companyDirectors, selectedDirectors);

        assertEquals("One or more officer IDs are not valid", result.get());
    }

    @Test
    public void areSelectedDirectorsValid_majorityOfDirectorsNotSelected_forOddNumberOfDirectors_returnsAnError() {
        final DirectorRequest selectedDirector = generateDirectorRequest();
        selectedDirector.setOfficerId(OFFICER_ID_ONE);

        final Map<String, CompanyOfficer> companyDirectors = Map.of(
                OFFICER_ID_ONE, generateCompanyOfficer(),
                OFFICER_ID_TWO, generateCompanyOfficer(),
                OFFICER_ID_THREE, generateCompanyOfficer()
        );

        final List<DirectorRequest> selectedDirectors = Collections.singletonList(selectedDirector);

        final Optional<String> result = validator.areSelectedDirectorsValid(companyDirectors, selectedDirectors);

        assertEquals("The majority of active company directors must be provided as signatories", result.get());
    }

    @Test
    public void areSelectedDirectorsValid_majorityOfDirectorsNotSelected_forEvenNumberOfDirectors_returnsAnError() {
        final DirectorRequest selectedDirector = generateDirectorRequest();
        selectedDirector.setOfficerId(OFFICER_ID_ONE);

        final Map<String, CompanyOfficer> companyDirectors = Map.of(
                OFFICER_ID_ONE, generateCompanyOfficer(),
                OFFICER_ID_TWO, generateCompanyOfficer()
        );

        final List<DirectorRequest> selectedDirectors = Collections.singletonList(selectedDirector);

        final Optional<String> result = validator.areSelectedDirectorsValid(companyDirectors, selectedDirectors);

        assertEquals("The majority of active company directors must be provided as signatories", result.get());
    }

    @Test
    public void areSelectedDirectorsValid_allRulesSatisfied_returnsNoError() {
        final DirectorRequest selectedDirector1 = generateDirectorRequest();
        selectedDirector1.setOfficerId(OFFICER_ID_ONE);
        selectedDirector1.setEmail(DIRECTOR_EMAIL_ONE);

        final DirectorRequest selectedDirector2 = generateDirectorRequest();
        selectedDirector2.setOfficerId(OFFICER_ID_TWO);
        selectedDirector2.setEmail(DIRECTOR_EMAIL_TWO);

        final Map<String, CompanyOfficer> companyDirectors = Map.of(
                OFFICER_ID_ONE, generateCompanyOfficer(),
                OFFICER_ID_TWO, generateCompanyOfficer(),
                OFFICER_ID_THREE, generateCompanyOfficer()
        );

        final List<DirectorRequest> selectedDirectors = Arrays.asList(selectedDirector1, selectedDirector2);

        final Optional<String> result = validator.areSelectedDirectorsValid(companyDirectors, selectedDirectors);

        assertTrue(result.isEmpty());
    }
}
