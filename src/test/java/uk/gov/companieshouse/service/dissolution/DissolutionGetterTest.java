package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionDirector;

@ExtendWith(MockitoExtension.class)
public class DissolutionGetterTest {

    @InjectMocks
    private DissolutionGetter getter;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionResponseMapper responseMapper;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String APPLICATION_REFERENCE = "XYZ456";
    public static final String OFFICER_ID_ONE = "abc123";
    public static final String OFFICER_ID_TWO = "def456";
    private static final String EMAIL = "applicant@email.com";

    @Test
    public void getByCompanyNumber_findsDissolution_mapsToDissolutionResponse_returnsGetResponse() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(responseMapper.mapToDissolutionGetResponse(dissolution)).thenReturn(response);

        final Optional<DissolutionGetResponse> result = getter.getByCompanyNumber(COMPANY_NUMBER);

        verify(repository).findByCompanyNumber(COMPANY_NUMBER);
        verify(responseMapper).mapToDissolutionGetResponse(dissolution);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    public void getByCompanyNumber_doesNotFindDissolution_returnsOptionalEmpty() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final Optional<DissolutionGetResponse> result = getter.getByCompanyNumber(COMPANY_NUMBER);

        verify(repository).findByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getByApplicationReference_findsDissolution_mapsToDissolutionResponse_returnsGetResponse() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(dissolution));
        when(responseMapper.mapToDissolutionGetResponse(dissolution)).thenReturn(response);

        final Optional<DissolutionGetResponse> result = getter.getByApplicationReference(APPLICATION_REFERENCE);

        verify(repository).findByDataApplicationReference(APPLICATION_REFERENCE);
        verify(responseMapper).mapToDissolutionGetResponse(dissolution);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    public void getByApplicationReference_doesNotFindDissolution_returnsOptionalEmpty() {
        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.empty());

        final Optional<DissolutionGetResponse> result = getter.getByApplicationReference(APPLICATION_REFERENCE);

        verify(repository).findByDataApplicationReference(APPLICATION_REFERENCE);

        assertTrue(result.isEmpty());
    }

    @Test
    public void isDirectorPendingApproval_returnsFalse_whenOfficerIdNotFound() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director = generateDissolutionDirector();
        director.setOfficerId(OFFICER_ID_ONE);

        dissolution.getData().setDirectors(Collections.singletonList(director));

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.isDirectorPendingApproval(COMPANY_NUMBER, OFFICER_ID_TWO);

        assertFalse(result);
    }

    @Test
    public void isDirectorPendingApproval_returnsFalse_whenAlreadyApproved() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director = generateDissolutionDirector();
        director.setOfficerId(OFFICER_ID_ONE);
        director.setDirectorApproval(generateDirectorApproval());

        dissolution.getData().setDirectors(Collections.singletonList(director));

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.isDirectorPendingApproval(COMPANY_NUMBER, OFFICER_ID_ONE);

        assertFalse(result);
    }

    @Test
    public void isDirectorPendingApproval_returnsTrue_whenNotApproved() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director = generateDissolutionDirector();
        director.setOfficerId(OFFICER_ID_ONE);
        director.setDirectorApproval(null);

        dissolution.getData().setDirectors(Collections.singletonList(director));

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.isDirectorPendingApproval(COMPANY_NUMBER, OFFICER_ID_ONE);

        assertTrue(result);
    }

    @Test
    public void doesDirectorExist_returnsFalse_whenDirectorDoesNotExist() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director = generateDissolutionDirector();
        director.setOfficerId(OFFICER_ID_TWO);

        dissolution.getData().setDirectors(Collections.singletonList(director));

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.doesDirectorExist(COMPANY_NUMBER, OFFICER_ID_ONE);

        assertFalse(result);
    }

    @Test
    public void doesDirectorExist_returnsTrue_whenDirectorExists() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director = generateDissolutionDirector();
        director.setOfficerId(OFFICER_ID_ONE);

        dissolution.getData().setDirectors(Collections.singletonList(director));

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.doesDirectorExist(COMPANY_NUMBER, OFFICER_ID_ONE);

        assertTrue(result);
    }

    @Test
    public void doesEmailBelongToApplicant_returnsTrue_whenEmailMatches() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        dissolution.getCreatedBy().setEmail(EMAIL);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.doesEmailBelongToApplicant(COMPANY_NUMBER, EMAIL);

        assertTrue(result);
    }

    @Test
    public void doesEmailBelongToApplicant_returnsFalse_whenEmailDoesNotMatch() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        dissolution.getCreatedBy().setEmail(EMAIL+"asd");

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.doesEmailBelongToApplicant(COMPANY_NUMBER, EMAIL);

        assertFalse(result);
    }
}
