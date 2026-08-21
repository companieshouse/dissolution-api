package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalData;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateDraftResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.DissolutionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.util.TransactionHelper;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionPatchRequest;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentPatchRequest;

@ExtendWith(MockitoExtension.class)
class DissolutionServiceTest {

    @InjectMocks
    private DissolutionService service;

    @Mock
    private DissolutionCreator creator;

    @Mock
    private DissolutionGetter getter;

    @Mock
    private DissolutionPatcher patcher;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private TransactionHelper transactionHelper;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String APPLICATION_REFERENCE = "XYZ456";
    public static final String USER_ID = "123";
    public static final String IP = "192.168.0.1";
    public static final String EMAIL = "user@mail.com";
    public static final String OFFICER_ID = "abc123";
    private static final String DISSOLUTION_ID = "12345678";
    private static final String TRANSACTION_ID = "tx-id-123";

    @Test
    void create_createsADissolutionRequest_returnsCreateResponse() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final DissolutionCreateResponse response = DissolutionFixtures.generateDissolutionCreateResponse();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        final Map<String, CompanyOfficer> companyDirectors = Map.of(OFFICER_ID, generateCompanyOfficer());

        when(creator.create(body, company, companyDirectors, USER_ID, IP, EMAIL)).thenReturn(response);

        final DissolutionCreateResponse result = service.create(body, company, companyDirectors, USER_ID, IP, EMAIL);

        verify(creator).create(body, company, companyDirectors, USER_ID, IP, EMAIL);

        assertEquals(response, result);
    }

    @Test
    void doesDissolutionRequestExistForCompanyByCompanyNumber_returnsTrue_ifDissolutionForCompanyExists() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(DissolutionFixtures.generateDissolution()));

        final boolean result = service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result);
    }

    @Test
    void doesDissolutionRequestExistForCompanyByCompanyNumber_returnsFalse_ifDissolutionForCompanyDoesNotExist() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final boolean result = service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER);

        assertFalse(result);
    }

    @Test
    void doesDissolutionRequestExistForCompanyByApplicationReference_returnsTrue_ifDissolutionForCompanyExists() {
        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(DissolutionFixtures.generateDissolution()));

        final boolean result = service.doesDissolutionRequestExistForCompanyByApplicationReference(APPLICATION_REFERENCE);

        assertTrue(result);
    }

    @Test
    void doesDissolutionRequestExistForCompanyByApplicationReference_returnsFalse_ifDissolutionForCompanyDoesNotExist() {
        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.empty());

        final boolean result = service.doesDissolutionRequestExistForCompanyByApplicationReference(APPLICATION_REFERENCE);

        assertFalse(result);
    }

    @Test
    void getByCompanyNumber_returnsDissolutionGetResponse() {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = service.getByCompanyNumber(COMPANY_NUMBER);

        verify(getter).getByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void getByApplicationReference_returnsDissolutionGetResponse() {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(getter.getByApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = service.getByApplicationReference(APPLICATION_REFERENCE);

        verify(getter).getByApplicationReference(APPLICATION_REFERENCE);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void addDirectorApproval_returnsPatchResponse_ifDissolutionForCompanyExists() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        final Dissolution dissolution = aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withDirectors(aDissolutionDirector().withOfficerId(OFFICER_ID))
                .build();
        final DissolutionPatchResponse response = DissolutionFixtures.generateDissolutionPatchResponse();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(patcher.addDirectorApproval(dissolution, directorApprovalData)).thenReturn(response);

        final DissolutionPatchResponse result = service.addDirectorApproval(COMPANY_NUMBER, directorApprovalData);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    void addDirectorApproval_whenDissolutionDoesNotExist_throwsException() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final var exception = assertThrows(DissolutionNotFoundException.class,
                () -> service.addDirectorApproval(COMPANY_NUMBER, directorApprovalData));

        assertThat(exception.getMessage(),
                is("Dissolution Request not found for company number " + COMPANY_NUMBER));

        verify(patcher, never()).addDirectorApproval(any(), any());
    }

    @Test
    void updatePaymentAndSubmissionStatus_updatesPaymentAndSubmissionStatus_returnNothing() throws DissolutionNotFoundException {
        PaymentPatchRequest data = generatePaymentPatchRequest();

        service.handlePayment(data, APPLICATION_REFERENCE);

        verify(patcher).handlePayment(data, APPLICATION_REFERENCE);
    }

    @Test
    void givenGetDissolutionByIdCalled_whenValidId_returnsDissolution() {
        var dissolution = DissolutionFixtures.generateDissolution();
        dissolution.setId(DISSOLUTION_ID);

        when(repository.findById(DISSOLUTION_ID)).thenReturn(Optional.of(dissolution));

        final Optional<Dissolution> result = service.getDissolutionById(DISSOLUTION_ID);

        verify(repository).findById(DISSOLUTION_ID);
        assertTrue(result.isPresent());
        assertEquals(dissolution, result.get());
    }

    @Test
    void givenGetDissolutionByIdCalled_whenInvalidId_returnsEmptyOptional() {
        when(repository.findById(DISSOLUTION_ID)).thenReturn(Optional.empty());

        final Optional<Dissolution> result = service.getDissolutionById(DISSOLUTION_ID);

        verify(repository).findById(DISSOLUTION_ID);
        assertFalse(result.isPresent());
    }

    @Test
    void givenGetDissolutionForTransactionCalled_whenLinkedDissolutionExists_returnsDissolution() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        var dissolution = DissolutionFixtures.generateDissolution();
        dissolution.setId(DISSOLUTION_ID);
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        when(transactionHelper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID)).thenReturn(true);
        when(repository.findById(DISSOLUTION_ID)).thenReturn(Optional.of(dissolution));

        final Dissolution result = service.getDissolutionForTransaction(transaction, DISSOLUTION_ID);

        verify(repository).findById(DISSOLUTION_ID);
        assertEquals(dissolution, result);
    }

    @Test
    void givenGetDissolutionForTransactionCalled_whenDissolutionNotLinkedToTransaction_throwsException() {
        var dissolution = DissolutionFixtures.generateDissolution();
        dissolution.setId(DISSOLUTION_ID);
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        when(transactionHelper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID)).thenReturn(false);

        assertThrows(DissolutionNotLinkedToTransactionException.class,
                () -> service.getDissolutionForTransaction(transaction, DISSOLUTION_ID));
        verify(repository, never()).findById(DISSOLUTION_ID);
    }

    @Test
    void givenGetDissolutionForTransactionCalled_whenDissolutionDoesNotExist_throwsException() {
        var dissolution = DissolutionFixtures.generateDissolution();
        dissolution.setId(DISSOLUTION_ID);
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        when(transactionHelper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID)).thenReturn(true);
        when(repository.findById(DISSOLUTION_ID)).thenReturn(Optional.empty());

        final var exception = assertThrows(DissolutionNotFoundException.class,
                () -> service.getDissolutionForTransaction(transaction, DISSOLUTION_ID));

        assertThat(exception.getMessage(),
                is("No dissolution found with id " + DISSOLUTION_ID));
        verify(repository, times(1)).findById(DISSOLUTION_ID);
    }

    @Test
    void getPendingDissolution_callsDissolutionGetter_getPendingDissolution() {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();
        when(getter.getPendingDissolution(COMPANY_NUMBER)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = service.getPendingDissolution(COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void getDraftDissolution_callsDissolutionGetter_getDraftDissolution() {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();
        when(getter.getDraftDissolution(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = service.getDraftDissolution(USER_ID, COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void createDraft_createsDraftDissolution_returnsCreateDraftResponse() {
        final Transaction transaction = TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.OPEN).build();
        final DissolutionCreateDraftResponse response = new DissolutionCreateDraftResponse();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();

        when(creator.createDraft(transaction, company, USER_ID, IP, EMAIL)).thenReturn(response);

        final DissolutionCreateDraftResponse result = service.createDraft(transaction, company, USER_ID, IP, EMAIL);

        assertEquals(response, result);
    }

    @Test
    void doesDraftDissolutionExistForUserAndCompany_returnsTrue_ifDraftDissolutionForUserAndCompanyExists() {
        final Dissolution dissolution = DissolutionTestDataBuilder.aDissolution().withTransactionId(TRANSACTION_ID).withStatus(DissolutionStatus.DRAFT).build();
        when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        final boolean result = service.doesDraftDissolutionExistForUserAndCompany(USER_ID, COMPANY_NUMBER);

        assertTrue(result);
    }

    @Test
    void doesDraftDissolutionExistForUserAndCompany_returnsFalse_ifDraftDissolutionForUserAndCompanyDoesNotExist() {
        when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.empty());

        final boolean result = service.doesDraftDissolutionExistForUserAndCompany(USER_ID, COMPANY_NUMBER);

        assertFalse(result);
    }
}
