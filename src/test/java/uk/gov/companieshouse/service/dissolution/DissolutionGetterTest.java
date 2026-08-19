package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionApplication;
import uk.gov.companieshouse.model.db.dissolution.DissolutionData;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.DissolutionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionDirector;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;

@ExtendWith(MockitoExtension.class)
class DissolutionGetterTest {

    @InjectMocks
    private DissolutionGetter getter;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionResponseMapper responseMapper;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String USER_ID = "123";
    public static final String APPLICATION_REFERENCE = "XYZ456";
    public static final String OFFICER_ID_ONE = "abc123";
    public static final String OFFICER_ID_TWO = "def456";

    @Test
    void getByCompanyNumber_findsDissolution_mapsToDissolutionResponse_returnsGetResponse() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(responseMapper.mapToDissolutionGetResponse(dissolution)).thenReturn(response);

        final Optional<DissolutionGetResponse> result = getter.getByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void getByCompanyNumber_doesNotFindDissolution_returnsOptionalEmpty() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final Optional<DissolutionGetResponse> result = getter.getByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result.isEmpty());
    }

    @Test
    void getByApplicationReference_findsDissolution_mapsToDissolutionResponse_returnsGetResponse() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(dissolution));
        when(responseMapper.mapToDissolutionGetResponse(dissolution)).thenReturn(response);

        final Optional<DissolutionGetResponse> result = getter.getByApplicationReference(APPLICATION_REFERENCE);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void getByApplicationReference_doesNotFindDissolution_returnsOptionalEmpty() {
        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.empty());

        final Optional<DissolutionGetResponse> result = getter.getByApplicationReference(APPLICATION_REFERENCE);

        assertTrue(result.isEmpty());
    }

    @Test
    void isDirectorPendingApproval_returnsFalse_whenOfficerIdNotFound() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director = generateDissolutionDirector();
        director.setOfficerId(OFFICER_ID_ONE);

        dissolution.getData().setDirectors(Collections.singletonList(director));

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = getter.isDirectorPendingApproval(COMPANY_NUMBER, OFFICER_ID_TWO);

        assertFalse(result);
    }

    @Test
    void isDirectorPendingApproval_returnsFalse_whenAlreadyApproved() {
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
    void isDirectorPendingApproval_returnsTrue_whenNotApproved() {
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
    void getPendingDissolution_findsDissolution_mapsToDissolutionResponse_returnsGetResponse() {
        final Dissolution dissolution = DissolutionTestDataBuilder.aDissolution().withTransactionId(TRANSACTION_ID).withStatus(DissolutionStatus.DRAFT).build();
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse(TRANSACTION_ID, DissolutionStatus.DRAFT);

        when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(responseMapper.mapToDissolutionGetResponse(dissolution)).thenReturn(response);

        final Optional<DissolutionGetResponse> result = getter.getPendingDissolution(COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void getPendingDissolution_doesNotFindDissolution_returnsOptionalEmpty() {
        when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final Optional<DissolutionGetResponse> result = getter.getPendingDissolution(COMPANY_NUMBER);

        assertTrue(result.isEmpty());
    }

    @Test
    void getDraftDissolution_findsDraftDissolution_mapsToDissolutionResponse_returnsGetResponse() {
        final DissolutionData data = new DissolutionData();
        final DissolutionApplication application = new DissolutionApplication();
        application.setType(ApplicationType.DS01);
        data.setApplication(application);
        final Dissolution dissolution = DissolutionTestDataBuilder.aDissolution().withTransactionId(TRANSACTION_ID).withStatus(DissolutionStatus.DRAFT).withActive(false).withData(data).build();
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse(TRANSACTION_ID, DissolutionStatus.DRAFT);

        when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(responseMapper.mapToDissolutionGetResponse(dissolution)).thenReturn(response);

        final Optional<DissolutionGetResponse> result = getter.getDraftDissolution(USER_ID, COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void getDraftDissolution_doesNotFindDissolution_returnsOptionalEmpty() {
        when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.empty());

        final Optional<DissolutionGetResponse> result = getter.getDraftDissolution(USER_ID, COMPANY_NUMBER);

        assertTrue(result.isEmpty());
    }
}
