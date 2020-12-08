package uk.gov.companieshouse.service.dissolution.director;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
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
class DissolutionDirectorGetterTest {

    @InjectMocks
    private DissolutionDirectorGetter getter;

    @Mock
    private DissolutionRepository repository;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String OFFICER_ID_ONE = "abc123";
    public static final String OFFICER_ID_TWO = "def456";

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
