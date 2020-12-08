package uk.gov.companieshouse.service.dissolution.director;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionDirector;

@ExtendWith(MockitoExtension.class)
public class DissolutionDirectorGetterTest {

    @InjectMocks
    private DissolutionDirectorGetter getter;

    @Mock
    private DissolutionRepository repository;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String APPLICATION_REFERENCE = "XYZ456";
    public static final String OFFICER_ID_ONE = "abc123";
    public static final String OFFICER_ID_TWO = "def456";
    private static final String EMAIL = "applicant@email.com";

    @Test
    void doesEmailBelongToApplicant_returnsTrue_whenEmailMatches() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        dissolution.getCreatedBy().setEmail(EMAIL);

        final boolean result = getter.doesEmailBelongToApplicant(EMAIL, dissolution);

        assertTrue(result);
    }

    @Test
    void doesEmailBelongToApplicant_returnsFalse_whenEmailDoesNotMatch() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        dissolution.getCreatedBy().setEmail(EMAIL + "asd");

        final boolean result = getter.doesEmailBelongToApplicant(EMAIL, dissolution);

        assertFalse(result);
    }

    @Test
    void isDirectorPendingApprovalForDissolution_returnsFalse_whenDirectorHasApprovalObject() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        dissolution.getData().getDirectors().get(0).setDirectorApproval(new DirectorApproval());
        dissolution.getData().getDirectors().get(0).setOfficerId(OFFICER_ID_ONE);

        final boolean result = getter.isDirectorPendingApprovalForDissolution(OFFICER_ID_ONE, dissolution);

        assertFalse(result);
    }

    @Test
    void isDirectorPendingApprovalForDissolution_returnsTrue_whenDirectorDoesntHaveApprovalObject() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        dissolution.getData().getDirectors().get(0).setDirectorApproval(null);
        dissolution.getData().getDirectors().get(0).setOfficerId(OFFICER_ID_ONE);

        final boolean result = getter.isDirectorPendingApprovalForDissolution(OFFICER_ID_ONE, dissolution);

        assertTrue(result);
    }

    @Test
    void doesDirectorExist_returnsFalse_whenDirectorDoesNotExist() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director = generateDissolutionDirector();
        director.setOfficerId(OFFICER_ID_TWO);

        dissolution.getData().setDirectors(Collections.singletonList(director));

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.doesDirectorExist(COMPANY_NUMBER, OFFICER_ID_ONE);

        assertFalse(result);
    }

    @Test
    void doesDirectorExist_returnsTrue_whenDirectorExists() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director = generateDissolutionDirector();
        director.setOfficerId(OFFICER_ID_ONE);

        dissolution.getData().setDirectors(Collections.singletonList(director));

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.doesDirectorExist(COMPANY_NUMBER, OFFICER_ID_ONE);

        assertTrue(result);
    }
}
