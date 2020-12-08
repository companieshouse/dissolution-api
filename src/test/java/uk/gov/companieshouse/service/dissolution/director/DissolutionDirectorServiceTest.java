package uk.gov.companieshouse.service.dissolution.director;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorFixtures.generateDissolutionPatchDirectorRequest;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;

@ExtendWith(MockitoExtension.class)
public class DissolutionDirectorServiceTest {

    @InjectMocks
    private DissolutionDirectorService service;

    @Mock
    private DissolutionDirectorGetter getter;

    @Mock
    private DissolutionDirectorPatcher patcher;

    @Mock
    private DissolutionRepository repository;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String ON_BEHALF_NAME = "Mr Accountant";
    public static final String EMAIL = "user@mail.com";
    public static final String OFFICER_ID = "abc123";

    @Test
    void checkPatchDirectorConstraints_returnsErrorString_whenDirectorIsNotPendingApproval() throws DissolutionNotFoundException {
        Dissolution dissolution = generateDissolution();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(getter.isDirectorPendingApprovalForDissolution(OFFICER_ID, dissolution)).thenReturn(false);

        Optional<String> error = service.checkPatchDirectorConstraints(COMPANY_NUMBER, OFFICER_ID, EMAIL);

        assertEquals("Director is not pending approval", error.get());
    }

    @Test
    void checkPatchDirectorConstraints_returnsErrorString_whenDirectorIsEmailIsNotApplicant() throws DissolutionNotFoundException {
        Dissolution dissolution = generateDissolution();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(getter.isDirectorPendingApprovalForDissolution(OFFICER_ID, dissolution)).thenReturn(true);
        when(getter.doesEmailBelongToApplicant(EMAIL, dissolution)).thenReturn(false);

        Optional<String> error = service.checkPatchDirectorConstraints(COMPANY_NUMBER, OFFICER_ID, EMAIL);

        assertEquals("Only the applicant can update signatory", error.get());
    }

    @Test
    void checkPatchDirectorConstraints_OptionalEmpty_ChecksPass() throws DissolutionNotFoundException {
        Dissolution dissolution = generateDissolution();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(getter.isDirectorPendingApprovalForDissolution(OFFICER_ID, dissolution)).thenReturn(true);
        when(getter.doesEmailBelongToApplicant(EMAIL, dissolution)).thenReturn(true);

        Optional<String> error = service.checkPatchDirectorConstraints(COMPANY_NUMBER, OFFICER_ID, EMAIL);

        assertTrue(error.isEmpty());
    }

    @Test
    void checkPatchDirectorConstraints_throwsException_WhenDissolutionNotFound() throws DissolutionNotFoundException {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        DissolutionNotFoundException thrown = assertThrows(DissolutionNotFoundException.class, () -> {
            service.checkPatchDirectorConstraints(COMPANY_NUMBER, OFFICER_ID, EMAIL);
        });

        assertNotNull(thrown);
    }

    @Test
    void doesDirectorExist_callsDissolutionGetter_doesDirectorExist() {
        final String companyNumber = "12345678";
        final String officerId = OFFICER_ID;

        when(getter.doesDirectorExist(companyNumber, officerId)).thenReturn(true);

        final boolean result = service.doesDirectorExist(companyNumber, officerId);

        verify(getter).doesDirectorExist(companyNumber, officerId);

        assertTrue(result);
    }

    @Test
    void updateSignatory_updatesSignatory_returnsPatchResponse() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        body.setEmail(EMAIL);
        body.setOnBehalfName(ON_BEHALF_NAME);

        final DissolutionDirectorPatchResponse response = DissolutionFixtures.generateDissolutionDirectorPatchResponse();

        when(patcher.updateSignatory(COMPANY_NUMBER, body, EMAIL)).thenReturn(response);

        final DissolutionDirectorPatchResponse result = service.updateSignatory(COMPANY_NUMBER, body, EMAIL);

        verify(patcher).updateSignatory(COMPANY_NUMBER, body, EMAIL);

        assertNotNull(result);
        assertEquals(response, result);
    }
}
