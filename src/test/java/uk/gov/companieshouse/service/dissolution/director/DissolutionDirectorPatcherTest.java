package uk.gov.companieshouse.service.dissolution.director;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.mapper.DissolutionDirectorResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorFixtures.generateDissolutionPatchDirectorRequest;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorPatchRequestTestDataBuilder.aDissolutionDirectorPatchRequest;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;

@ExtendWith(MockitoExtension.class)
class DissolutionDirectorPatcherTest {

    @InjectMocks
    private DissolutionDirectorPatcher patcher;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionDirectorResponseMapper directorResponseMapper;

    @Mock
    private DissolutionEmailService dissolutionEmailService;

    private static final String COMPANY_NUMBER = "12345678";
    private static final String OFFICER_ID = "abc123";
    private static final String EMAIL = "mail@mail.com";
    private static final String ON_BEHALF_NAME = "on behalf name";

    private Dissolution dissolution;
    private DissolutionDirectorPatchResponse directorResponse;
    private ArgumentCaptor<Dissolution> dissolutionCaptor;

    @BeforeEach
    void init() {
        dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getData().getDirectors().getFirst().setOfficerId(OFFICER_ID);
        directorResponse = DissolutionFixtures.generateDissolutionDirectorPatchResponse();
        dissolutionCaptor = ArgumentCaptor.forClass(Dissolution.class);
    }

    @Test
    void patch_updateSignatory_updatesSignatoryWithEmail_SavesInDatabaseAndSendsEmail() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();

        body.setEmail(EMAIL);
        body.setOnBehalfName(null);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID);

        verify(dissolutionEmailService).notifySignatoryToSign(dissolutionCaptor.capture(), eq(EMAIL));
        verify(repository).save(dissolutionCaptor.capture());

        assertEquals(EMAIL, dissolutionCaptor.getValue().getData().getDirectors().getFirst().getEmail());
        assertNull(null, dissolutionCaptor.getValue().getData().getDirectors().getFirst().getOnBehalfName());
    }

    @Test
    void patch_updateSignatory_updatesSignatoryWithEmailAndOnBehalfName_SavesInDatabaseAndSendsEmail() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        dissolution.getData().getDirectors().getFirst().setEmail(EMAIL + "asd");

        body.setEmail(EMAIL);
        body.setOnBehalfName(ON_BEHALF_NAME);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID);

        verify(dissolutionEmailService).notifySignatoryToSign(dissolutionCaptor.capture(), eq(EMAIL));
        verify(repository).save(dissolutionCaptor.capture());

        assertEquals(EMAIL, dissolutionCaptor.getValue().getData().getDirectors().getFirst().getEmail());
        assertEquals(ON_BEHALF_NAME, dissolutionCaptor.getValue().getData().getDirectors().getFirst().getOnBehalfName());
    }

    @Test
    void patch_updateSignatory_updatesSignatoryWithTheSameEmailButDifferentName_SavesInDatabaseAndSendsEmail() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        dissolution.getData().getDirectors().getFirst().setEmail(EMAIL);
        dissolution.getData().getDirectors().getFirst().setOnBehalfName(null);

        body.setEmail(EMAIL);
        body.setOnBehalfName(ON_BEHALF_NAME);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(directorResponseMapper.mapToDissolutionDirectorPatchResponse(dissolution)).thenReturn(directorResponse);

        final DissolutionDirectorPatchResponse result = patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID);

        verify(directorResponseMapper).mapToDissolutionDirectorPatchResponse(dissolution);
        verify(dissolutionEmailService).notifySignatoryToSign(dissolutionCaptor.capture(), eq(EMAIL));
        verify(repository).save(dissolutionCaptor.capture());

        assertEquals(directorResponse, result);
        assertEquals(EMAIL, dissolutionCaptor.getValue().getData().getDirectors().getFirst().getEmail());
        assertEquals(ON_BEHALF_NAME, dissolutionCaptor.getValue().getData().getDirectors().getFirst().getOnBehalfName());
    }

    @Test
    void patch_updateSignatory_updatesSignatoryWithTheSameEmailAndOnBehalfName_DoesNotSaveInDatabaseAndDoesNotSendEmail() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        dissolution.getData().getDirectors().getFirst().setEmail(EMAIL);
        dissolution.getData().getDirectors().getFirst().setOnBehalfName(ON_BEHALF_NAME);

        body.setEmail(EMAIL);
        body.setOnBehalfName(ON_BEHALF_NAME);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID);

        verify(dissolutionEmailService, times(0)).notifySignatoryToSign(any(), any());
        verify(repository, times(0)).save(any());
    }

    @Test
    void patch_updateSignatory_updatesSignatoryWithTheSameEmailAndBothNullOnBehalfName_DoesNotSaveInDatabaseAndDoesNotSendEmail() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        dissolution.getData().getDirectors().getFirst().setEmail(EMAIL);
        dissolution.getData().getDirectors().getFirst().setOnBehalfName(null);

        body.setEmail(EMAIL);
        body.setOnBehalfName(null);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID);

        verify(dissolutionEmailService, times(0)).notifySignatoryToSign(any(), any());
        verify(repository, times(0)).save(any());
    }

    @Test
    void patch_updateSignatory_throwsExceptionWhenDirectorNotFound_DoesNotSaveInDatabaseAndDoesNotSendEmail() {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        setExistingDirector(dissolution, aDissolutionDirector().withOfficerId("random"));

        body.setEmail(EMAIL);
        body.setOnBehalfName(null);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        DissolutionNotFoundException exception = assertThrows(
                DissolutionNotFoundException.class,
                () -> patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID)
        );

        verify(dissolutionEmailService, times(0)).notifySignatoryToSign(any(), any());
        verify(repository, times(0)).save(any());

        assertNotNull(exception);
    }

    @DisplayName("Normalizes fields:")
    @ParameterizedTest(name = "{index} => {0}")
    @CsvSource(
            delimiter = '|',
            quoteCharacter = '\'',
            value = {
                    "'email is trimmed'    | ' mail@mail.com '  | 'mail@mail.com'",
                    "'email is lowercased' | 'MAIL@MAIL.COM'    | 'mail@mail.com'",
            }
    )
    void update_data_normalization_test(
            String description,
            String requestEmail,
            String expectedStoredEmail
    ) throws DissolutionNotFoundException {
        final var dissolutionDirectorPatchRequest = aDissolutionDirectorPatchRequest()
                .withEmail(requestEmail)
                .build();

        final var existingDirector = aDissolutionDirector()
                .withOfficerId(OFFICER_ID)
                .withEmail("old@mail.com")
                .withOnBehalfName("Existing Name");

        setExistingDirector(dissolution, existingDirector);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        patcher.updateSignatory(COMPANY_NUMBER, dissolutionDirectorPatchRequest, OFFICER_ID);

        verify(repository).save(dissolutionCaptor.capture());

        final var savedDirector = dissolutionCaptor.getValue().getData().getDirectors().getFirst();
        assertEquals(expectedStoredEmail, savedDirector.getEmail());
    }

    private void setExistingDirector(Dissolution dissolution,
                                     DissolutionDirectorTestDataBuilder directorBuilder) {
        dissolution.getData().setDirectors(Collections.singletonList(directorBuilder.build()));
    }
}
