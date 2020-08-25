package uk.gov.companieshouse.service.dissolution.validator;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorRequest;

public class CompanyOfficerValidatorTest {

    private static final String OFFICER_ID_ONE = "abc123";
    private static final String OFFICER_ID_TWO = "def456";
    private static final String OFFICER_ID_THREE = "ghi789";

    private final CompanyOfficerValidator validator = new CompanyOfficerValidator();

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

        final DirectorRequest selectedDirector2 = generateDirectorRequest();
        selectedDirector2.setOfficerId(OFFICER_ID_TWO);

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
