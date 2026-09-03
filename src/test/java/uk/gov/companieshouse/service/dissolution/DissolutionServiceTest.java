package uk.gov.companieshouse.service.dissolution;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.DissolutionUpdateSignatoryException;
import uk.gov.companieshouse.exception.DissolutionInvalidSignatoriesException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.DissolutionSignatoryNotFoundException;
import uk.gov.companieshouse.exception.InvalidTransactionStateException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.domain.CreateDraftDissolutionCommand;
import uk.gov.companieshouse.model.domain.UpdateSignatoryDetailsCommand;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalCommand;
import uk.gov.companieshouse.model.domain.ResendSignatoryNotificationCommand;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateDraftResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.CompanyOfficerService;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.payment.PaymentService;
import uk.gov.companieshouse.service.transaction.TransactionFiling;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.CLOSED;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.OPEN;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;
import static uk.gov.companieshouse.fixtures.CompanyOfficerTestDataBuilder.aCompanyOfficer;
import static uk.gov.companieshouse.fixtures.DirectorRequestTestDataBuilder.aDirectorRequest;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorPatchRequestTestDataBuilder.aDissolutionDirectorPatchRequest;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateCreatedBy;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionPatchRequest;
import static uk.gov.companieshouse.fixtures.DissolutionGetResponseTestDataBuilder.aDissolutionGetResponse;
import static uk.gov.companieshouse.fixtures.DissolutionInitiationCommandTestDataBuilder.aDissolutionInitiationCommand;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentPatchRequest;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.generateTransactionResource;
import static uk.gov.companieshouse.fixtures.TransactionTestDataBuilder.aTransaction;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.enums.DissolutionStatus.DRAFT;
import static uk.gov.companieshouse.model.enums.DissolutionStatus.PENDING;
import static uk.gov.companieshouse.model.enums.DissolutionStatus.SUBMITTED;
import static uk.gov.companieshouse.util.EricHelper.getEmail;

@ExtendWith(MockitoExtension.class)
class DissolutionServiceTest {

    private DissolutionService dissolutionService;

    @Mock
    private DissolutionCreator creator;

    @Mock
    private DissolutionGetter getter;

    @Mock
    private DissolutionPatcher patcher;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TransactionService transactionService;

    private final DissolutionResponseMapper responseMapper = new DissolutionResponseMapper();
    private final DissolutionRequestMapper requestMapper = new DissolutionRequestMapper();

    @Mock
    private Logger logger;

    @Mock
    private CompanyOfficerService companyOfficerService;

    @Mock
    private DissolutionEmailService emailService;

    @Captor
    private ArgumentCaptor<Dissolution> dissolutionCaptor;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String APPLICATION_REFERENCE = "XYZ456";
    public static final String USER_ID = "123";
    public static final String IP = "192.168.0.1";
    public static final String EMAIL = "user@mail.com";
    public static final String OFFICER_ID = "abc123";
    private static final String DISSOLUTION_ID = "12345678";
    private static final String TRANSACTION_ID = "tx-id-123";
    private static final String PAYMENT_REFERENCE = "payment-ref-123";

    @BeforeEach
    void setUp() {
        dissolutionService = new DissolutionService(creator, getter, patcher, repository, paymentService, transactionService, responseMapper, logger, companyOfficerService, requestMapper, emailService);
    }

    @Test
    void create_createsADissolutionRequest_returnsCreateResponse() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final DissolutionCreateResponse response = DissolutionFixtures.generateDissolutionCreateResponse();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        final Map<String, CompanyOfficer> companyDirectors = Map.of(OFFICER_ID, generateCompanyOfficer());

        when(creator.create(body, company, companyDirectors, USER_ID, IP, EMAIL)).thenReturn(response);

        final DissolutionCreateResponse result = dissolutionService.create(body, company, companyDirectors, USER_ID, IP, EMAIL);

        verify(creator).create(body, company, companyDirectors, USER_ID, IP, EMAIL);

        assertEquals(response, result);
    }

    @Test
    void doesDissolutionRequestExistForCompanyByCompanyNumber_returnsTrue_ifDissolutionForCompanyExists() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(DissolutionFixtures.generateDissolution()));

        final boolean result = dissolutionService.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result);
    }

    @Test
    void doesDissolutionRequestExistForCompanyByCompanyNumber_returnsFalse_ifDissolutionForCompanyDoesNotExist() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final boolean result = dissolutionService.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER);

        assertFalse(result);
    }

    @Test
    void doesDissolutionRequestExistForCompanyByApplicationReference_returnsTrue_ifDissolutionForCompanyExists() {
        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(DissolutionFixtures.generateDissolution()));

        final boolean result = dissolutionService.doesDissolutionRequestExistForCompanyByApplicationReference(APPLICATION_REFERENCE);

        assertTrue(result);
    }

    @Test
    void doesDissolutionRequestExistForCompanyByApplicationReference_returnsFalse_ifDissolutionForCompanyDoesNotExist() {
        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.empty());

        final boolean result = dissolutionService.doesDissolutionRequestExistForCompanyByApplicationReference(APPLICATION_REFERENCE);

        assertFalse(result);
    }

    @Test
    void getByCompanyNumber_returnsDissolutionGetResponse() {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = dissolutionService.findActiveDissolution(COMPANY_NUMBER);

        verify(getter).getByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void getByApplicationReference_returnsDissolutionGetResponse() {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(getter.getByApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = dissolutionService.getByApplicationReference(APPLICATION_REFERENCE);

        verify(getter).getByApplicationReference(APPLICATION_REFERENCE);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void addDirectorApproval_returnsPatchResponse_ifDissolutionForCompanyExists() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalCommand(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        final Dissolution dissolution = aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withDirectors(aDissolutionDirector().withOfficerId(OFFICER_ID))
                .build();
        final DissolutionPatchResponse response = DissolutionFixtures.generateDissolutionPatchResponse();

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(patcher.addDirectorApproval(dissolution, directorApprovalData)).thenReturn(response);

        final DissolutionPatchResponse result = dissolutionService.addDirectorApproval(COMPANY_NUMBER, directorApprovalData);

        assertNotNull(result);
        assertEquals(response, result);

        verify(repository, never()).findPendingDissolutionByCompanyNumber(COMPANY_NUMBER);
    }

    @Test
    void addDirectorApproval_whenDissolutionDoesNotExist_throwsException() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalCommand(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final var exception = assertThrows(DissolutionNotFoundException.class,
                () -> dissolutionService.addDirectorApproval(COMPANY_NUMBER, directorApprovalData));

        assertThat(exception.getMessage(),
                is("Dissolution Request not found for company number " + COMPANY_NUMBER));

        verify(repository, never()).findPendingDissolutionByCompanyNumber(COMPANY_NUMBER);
        verify(patcher, never()).addDirectorApproval(any(), any());
    }

    @Test
    void updatePaymentAndSubmissionStatus_updatesPaymentAndSubmissionStatus_returnNothing() throws DissolutionNotFoundException {
        PaymentPatchRequest data = generatePaymentPatchRequest();

        dissolutionService.handlePayment(data, APPLICATION_REFERENCE);

        verify(patcher).handlePayment(data, APPLICATION_REFERENCE);
    }

    @Test
    void givenGetDissolutionByIdCalled_whenLinkedDissolutionExists_returnsDissolution() {
        final var dissolution = aDissolution().withId(DISSOLUTION_ID).build();

        when(repository.findById(DISSOLUTION_ID)).thenReturn(Optional.of(dissolution));

        final Dissolution result = dissolutionService.getDissolutionById(DISSOLUTION_ID);

        verify(repository).findById(DISSOLUTION_ID);
        assertEquals(dissolution, result);
    }

    @Test
    void givenGetDissolutionByIdCalled_whenDissolutionDoesNotExist_throwsException() {
        when(repository.findById(DISSOLUTION_ID)).thenReturn(Optional.empty());

        final var exception = assertThrows(DissolutionNotFoundException.class,
                () -> dissolutionService.getDissolutionById(DISSOLUTION_ID));

        assertThat(exception.getMessage(),
                is("No dissolution found with id " + DISSOLUTION_ID));
        verify(repository, times(1)).findById(DISSOLUTION_ID);
    }

    @Test
    void getPendingDissolution_callsDissolutionGetter_findPendingDissolution() {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();
        when(getter.getPendingDissolution(COMPANY_NUMBER)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = dissolutionService.findPendingDissolution(COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void getDraftDissolution_callsDissolutionGetter_findDraftDissolution() {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();
        when(getter.getDraftDissolution(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = dissolutionService.findDraftDissolution(USER_ID, COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    void createDraft_createsDraftDissolutionAndUpdatesTransaction_returnsCreateDraftResponse() {
        final Transaction transaction = aTransaction().withStatus(TransactionStatus.OPEN).withCompanyNumber(COMPANY_NUMBER).build();
        final String expectedSelfLink = String.format("/company/%s/transaction/%s/dissolution", COMPANY_NUMBER, transaction.getId());
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        Dissolution dissolution = aDissolution().
                withId(DISSOLUTION_ID).
                withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(company.getCompanyName())
                .withTransactionId(transaction.getId())
                .build();
        final var command = new CreateDraftDissolutionCommand(transaction, company, USER_ID, IP, EMAIL);
        final var filing = new TransactionFiling(dissolution.getId(), FILING_KIND_DS01, company.getCompanyName());

        when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.empty());
        when(creator.createDraft(command)).thenReturn(dissolution);

        final DissolutionCreateDraftResponse result = dissolutionService.createDraft(command);


        assertEquals(DISSOLUTION_ID, result.getDissolutionId());
        assertEquals(expectedSelfLink, result.getLinks().getSelf());

        verify(repository).insert(dissolution);
        verify(transactionService).updateTransaction(transaction, filing);
    }

    @Test
    void createDraft_rollsBackInsert_andRethrows_ifUpdateTransactionFails() {
        final Transaction transaction = aTransaction().withStatus(TransactionStatus.OPEN).withCompanyNumber(COMPANY_NUMBER).build();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        final Dissolution dissolution = aDissolution()
                .withId(DISSOLUTION_ID)
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(company.getCompanyName())
                .withTransactionId(transaction.getId())
                .build();

        final var command = new CreateDraftDissolutionCommand(transaction, company, USER_ID, IP, EMAIL);
        final var filing = new TransactionFiling(dissolution.getId(), FILING_KIND_DS01, company.getCompanyName());

        when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.empty());
        when(creator.createDraft(command)).thenReturn(dissolution);
        doThrow(new ServiceException("Failed to update transaction")).when(transactionService).updateTransaction(transaction, filing);

        assertThrows(ServiceException.class, () -> dissolutionService.createDraft(command));

        verify(repository).insert(dissolution);
        verify(repository).deleteById(DISSOLUTION_ID);
    }

    @Test
    void createDraft_returnsConflict_ifDraftDissolutionAlreadyExistsForUserAndCompany() {
        final Transaction transaction = aTransaction().withStatus(TransactionStatus.OPEN).withCompanyNumber(COMPANY_NUMBER).build();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        final var command = new CreateDraftDissolutionCommand(transaction, company, USER_ID, IP, EMAIL);

        when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.of(new Dissolution()));

        assertThrows(ConflictException.class, () -> dissolutionService.createDraft(command));

        verify(creator, never()).createDraft(any());
    }

    @Test
    void createDraft_returnsInvalidTransactionStateException_ifTransactionIsNotOpen() {
        final Transaction transaction = aTransaction().withStatus(TransactionStatus.CLOSED).withCompanyNumber(COMPANY_NUMBER).build();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        final var command = new CreateDraftDissolutionCommand(transaction, company, USER_ID, IP, EMAIL);

        when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.empty());

        assertThrows(InvalidTransactionStateException.class, () -> dissolutionService.createDraft(command));

        verify(creator, never()).createDraft(any());
    }

    @Test
    void createDraft_returnsInvalidTransactionStateException_ifTransactionIsLinkedToAnotherCompany() {
        final var wrongCompanyNumber = "87654321";
        final Transaction transaction = aTransaction().withStatus(TransactionStatus.OPEN).withCompanyNumber(wrongCompanyNumber).build();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        final var command = new CreateDraftDissolutionCommand(transaction, company, USER_ID, IP, EMAIL);

        when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.empty());

        assertThrows(InvalidTransactionStateException.class, () -> dissolutionService.createDraft(command));

        verify(creator, never()).createDraft(any());
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class addDirectorApprovalForTransactionModelDissolution {

        private Dissolution dissolution;
        private DissolutionDirectorApprovalCommand directorApprovalData;

        @BeforeEach
        void initialize() {
            final DissolutionPatchRequest body = generateDissolutionPatchRequest();
            body.setIpAddress(IP);
            body.setOfficerId(OFFICER_ID);
            directorApprovalData = new DissolutionDirectorApprovalCommand(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

            dissolution = aDissolution()
                    .withId(DISSOLUTION_ID)
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withDirectors(aDissolutionDirector().withOfficerId(OFFICER_ID))
                    .build();
        }

        @Test
        void when_pending_dissolution_exists_then_director_approval_is_applied() {
            final Transaction transaction = aTransaction()
                    .withStatus(TransactionStatus.OPEN)
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                    .build();

            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

            dissolutionService.addDirectorApproval(COMPANY_NUMBER, transaction, directorApprovalData);

            verify(repository, never()).findByCompanyNumber(COMPANY_NUMBER);
            verify(patcher, times(1)).addDirectorApproval(dissolution, directorApprovalData);
        }

        @Test
        void when_pending_dissolution_does_not_exist_then_dissolution_not_found_exception_thrown() {
            final Transaction transaction = aTransaction()
                    .withStatus(TransactionStatus.OPEN)
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                    .build();

            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dissolutionService.addDirectorApproval(COMPANY_NUMBER, transaction, directorApprovalData))
                    .isInstanceOf(DissolutionNotFoundException.class)
                    .hasMessage("Pending Dissolution not found for company number " + COMPANY_NUMBER);

            verify(repository, never()).findByCompanyNumber(COMPANY_NUMBER);
            verify(patcher, never()).addDirectorApproval(any(), any());
        }

        @Test
        void when_transaction_is_closed_then_invalid_transaction_state_exception_thrown() {
            final Transaction transaction = aTransaction()
                    .withStatus(TransactionStatus.CLOSED)
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                    .build();

            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

            assertThatThrownBy(() -> dissolutionService.addDirectorApproval(COMPANY_NUMBER, transaction, directorApprovalData))
                    .isInstanceOf(InvalidTransactionStateException.class)
                    .hasMessage("Transaction status CLOSED does not match expected status OPEN");

            verify(repository, never()).findByCompanyNumber(COMPANY_NUMBER);
            verify(patcher, never()).addDirectorApproval(any(), any());
        }

        @Test
        void when_transaction_is_not_linked_to_company_then_invalid_transaction_state_exception_thrown() {
            final Transaction transaction = aTransaction()
                    .withStatus(TransactionStatus.OPEN)
                    .withCompanyNumber("87654321") // Different company number
                    .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                    .build();

            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

            assertThatThrownBy(() -> dissolutionService.addDirectorApproval(COMPANY_NUMBER, transaction, directorApprovalData))
                    .isInstanceOf(InvalidTransactionStateException.class)
                    .hasMessage("Transaction does not belong to company " + COMPANY_NUMBER);

            verify(repository, never()).findByCompanyNumber(COMPANY_NUMBER);
            verify(patcher, never()).addDirectorApproval(any(), any());
        }

        @Test
        void when_transaction_is_not_linked_to_dissolution_then_dissolution_not_linked_to_transaction_exception_thrown() {
            final Transaction transaction = aTransaction()
                    .withStatus(TransactionStatus.OPEN)
                    .withCompanyNumber(COMPANY_NUMBER)
                    .build();

            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

            assertThatThrownBy(() -> dissolutionService.addDirectorApproval(COMPANY_NUMBER, transaction, directorApprovalData))
                    .isInstanceOf(DissolutionNotLinkedToTransactionException.class)
                    .hasMessage("Transaction is not linked to dissolution " + DISSOLUTION_ID);

            verify(repository, never()).findByCompanyNumber(COMPANY_NUMBER);
            verify(patcher, never()).addDirectorApproval(any(), any());
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class getSubmittedDissolutionWithNoVerdict {

        @Test
        void when_no_submitted_dissolution_exists_then_returns_empty() {
            when(repository.findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(COMPANY_NUMBER, SUBMITTED)).thenReturn(Optional.empty());

            var result = dissolutionService.findSubmittedDissolutionWithNoVerdict(COMPANY_NUMBER);

            assertThat(result).isEmpty();
        }

        @Test
        void when_submitted_dissolution_has_no_verdict_then_returns_it() {
            var dissolution = aDissolution().withTransactionId(TRANSACTION_ID).withApplicationReference(APPLICATION_REFERENCE).build();
            when(repository.findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(COMPANY_NUMBER, SUBMITTED)).thenReturn(Optional.of(dissolution));
            when(transactionService.hasVerdictBeenReached(TRANSACTION_ID)).thenReturn(false);

            var result = dissolutionService.findSubmittedDissolutionWithNoVerdict(COMPANY_NUMBER);

            assertThat(result.get().getApplicationReference()).isEqualTo(APPLICATION_REFERENCE);
        }

        @Test
        void when_submitted_dissolution_already_has_a_verdict_then_returns_empty() {
            var dissolution = aDissolution().withTransactionId(TRANSACTION_ID).build();
            when(repository.findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(COMPANY_NUMBER, SUBMITTED)).thenReturn(Optional.of(dissolution));
            when(transactionService.hasVerdictBeenReached(TRANSACTION_ID)).thenReturn(true);

            var result = dissolutionService.findSubmittedDissolutionWithNoVerdict(COMPANY_NUMBER);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class resolveDissolutionApplication {

        @Test
        void when_found_by_company_number_then_returns_it_without_falling_back() {
            when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(of(aDissolutionGetResponse()
                    .withApplicationReference(APPLICATION_REFERENCE)
                    .build()));

            var dissolutionDto = dissolutionService.resolveDissolutionApplication(USER_ID, COMPANY_NUMBER);

            assertThat(dissolutionDto.get().getApplicationReference()).isEqualTo(APPLICATION_REFERENCE);
        }

        @Test
        void when_not_found_by_company_number_then_falls_back_to_pending_dissolution() {
            when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(empty());
            when(getter.getPendingDissolution(COMPANY_NUMBER)).thenReturn(of(aDissolutionGetResponse()
                    .withDissolutionStatus(PENDING)
                    .build()));

            var dissolutionDto = dissolutionService.resolveDissolutionApplication(USER_ID, COMPANY_NUMBER);

            assertThat(dissolutionDto.get().getDissolutionStatus()).isEqualTo(PENDING);
        }

        @Test
        void when_not_found_by_pending_then_falls_back_to_submitted_dissolution_with_no_verdict() {
            when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(empty());
            when(getter.getPendingDissolution(COMPANY_NUMBER)).thenReturn(empty());
            when(repository.findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(COMPANY_NUMBER, SUBMITTED)).thenReturn(of(aDissolution()
                    .withTransactionId(TRANSACTION_ID)
                    .withStatus(SUBMITTED)
                    .build()));
            when(transactionService.hasVerdictBeenReached(TRANSACTION_ID)).thenReturn(false);

            var dissolutionDto = dissolutionService.resolveDissolutionApplication(USER_ID, COMPANY_NUMBER);

            assertThat(dissolutionDto.get().getDissolutionStatus()).isEqualTo(SUBMITTED);
        }

        @Test
        void when_submitted_dissolution_already_has_a_verdict_then_falls_back_to_draft_dissolution() {
            when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(empty());
            when(getter.getPendingDissolution(COMPANY_NUMBER)).thenReturn(empty());
            when(repository.findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(COMPANY_NUMBER, SUBMITTED)).thenReturn(of(aDissolution()
                    .withTransactionId(TRANSACTION_ID)
                    .build()));
            when(transactionService.hasVerdictBeenReached(TRANSACTION_ID)).thenReturn(true);
            when(getter.getDraftDissolution(USER_ID, COMPANY_NUMBER)).thenReturn(of(aDissolutionGetResponse()
                    .withDissolutionStatus(DRAFT)
                    .build()));

            var dissolutionDto = dissolutionService.resolveDissolutionApplication(USER_ID, COMPANY_NUMBER);

            assertThat(dissolutionDto.get().getDissolutionStatus()).isEqualTo(DRAFT);
        }

        @Test
        void when_not_found_by_submitted_then_falls_back_to_draft_dissolution() {
            when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(empty());
            when(getter.getPendingDissolution(COMPANY_NUMBER)).thenReturn(empty());
            when(repository.findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(COMPANY_NUMBER, SUBMITTED)).thenReturn(empty());
            when(getter.getDraftDissolution(USER_ID, COMPANY_NUMBER)).thenReturn(of(aDissolutionGetResponse()
                    .withDissolutionStatus(DRAFT)
                    .build()));

            var dissolutionDto = dissolutionService.resolveDissolutionApplication(USER_ID, COMPANY_NUMBER);

            assertThat(dissolutionDto.get().getDissolutionStatus()).isEqualTo(DRAFT);
        }

        @Test
        void when_no_dissolution_found_by_company_number_pending_submitted_or_draft_then_empty() {
            when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(empty());
            when(getter.getPendingDissolution(COMPANY_NUMBER)).thenReturn(empty());
            when(repository.findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(COMPANY_NUMBER, SUBMITTED)).thenReturn(empty());
            when(getter.getDraftDissolution(USER_ID, COMPANY_NUMBER)).thenReturn(empty());

            var dissolutionDto = dissolutionService.resolveDissolutionApplication(USER_ID, COMPANY_NUMBER);

            assertThat(dissolutionDto).isEmpty();
        }

        @Test
        void when_pending_payment_and_payment_accepted_then_application_status_is_set_to_paid() throws DissolutionNotFoundException {
            when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(of(aDissolutionGetResponse()
                    .withApplicationStatus(ApplicationStatus.PENDING_PAYMENT)
                    .withPaymentReference(PAYMENT_REFERENCE)
                    .build()));
            when(paymentService.getPaymentStatus(PAYMENT_REFERENCE)).thenReturn("accepted");

            var dissolutionDto = dissolutionService.resolveDissolutionApplication(USER_ID, COMPANY_NUMBER);

            assertThat(dissolutionDto.get().getApplicationStatus()).isEqualTo(ApplicationStatus.PAID);
        }

        @Test
        void when_pending_payment_and_payment_status_unavailable_then_payment_reference_is_reset() throws DissolutionNotFoundException {
            when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(of(aDissolutionGetResponse()
                    .withApplicationStatus(ApplicationStatus.PENDING_PAYMENT)
                    .withPaymentReference(PAYMENT_REFERENCE)
                    .withApplicationReference(APPLICATION_REFERENCE)
                    .build()));
            when(paymentService.getPaymentStatus(PAYMENT_REFERENCE)).thenReturn(null);

            var dissolutionDto = dissolutionService.resolveDissolutionApplication(USER_ID, COMPANY_NUMBER);

            assertThat(dissolutionDto.get().getApplicationStatus()).isEqualTo(ApplicationStatus.PENDING_PAYMENT);
            verify(patcher).setPaymentReference("", APPLICATION_REFERENCE);
        }

        @Test
        void when_resetting_payment_reference_fails_because_dissolution_no_longer_exists_then_throws_not_found() throws DissolutionNotFoundException {
            when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(of(aDissolutionGetResponse()
                    .withApplicationStatus(ApplicationStatus.PENDING_PAYMENT)
                    .withPaymentReference(PAYMENT_REFERENCE)
                    .withApplicationReference(APPLICATION_REFERENCE)
                    .build()));
            when(paymentService.getPaymentStatus(PAYMENT_REFERENCE)).thenReturn(null);
            doThrow(new DissolutionNotFoundException("not found")).when(patcher).setPaymentReference("", APPLICATION_REFERENCE);

            assertThrows(NotFoundException.class,
                    () -> dissolutionService.resolveDissolutionApplication(USER_ID, COMPANY_NUMBER));
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class initiateDissolution {

        private final Transaction transaction = aTransaction()
                .withId(TRANSACTION_ID)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();

        @Test
        void when_transaction_validation_fails_then_exception_thrown() {

            final var command = aDissolutionInitiationCommand()
                    .withTransaction(aTransaction().withId(TRANSACTION_ID).withStatus(TransactionStatus.CLOSED)).build();

            when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER))
                    .thenReturn(of(aDissolution().withId(DISSOLUTION_ID).withTransactionId(TRANSACTION_ID).build()));

            assertThrows(InvalidTransactionStateException.class,
                    () -> dissolutionService.initiateDissolution(command));
        }

        @Test
        void when_a_pending_dissolution_already_exists_for_the_company_then_exception_thrown() {
            when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER))
                    .thenReturn(of(aDissolution().withId(DISSOLUTION_ID).withTransactionId(TRANSACTION_ID).build()));
            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER))
                    .thenReturn(of(aDissolution().withTransactionId(TRANSACTION_ID).build()));

            final var command = aDissolutionInitiationCommand().withTransaction(transaction).build();

            assertThrows(ConflictException.class,
                    () -> dissolutionService.initiateDissolution(command));
        }

        @Test
        void when_DRAFT_dissolution_for_user_and_company_does_not_exist_then_exception_thrown() {
            when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(empty());

            final var command = aDissolutionInitiationCommand().build();

            assertThrows(DissolutionNotFoundException.class,
                    () -> dissolutionService.initiateDissolution(command));
        }

        @Test
        void when_DRAFT_dissolution_belongs_to_a_different_transaction_then_exception_thrown() {
            when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER))
                    .thenReturn(of(aDissolution().withId("some-other-dissolution-id").withTransactionId(TRANSACTION_ID).build()));

            final var command = aDissolutionInitiationCommand().withTransaction(transaction).build();

            assertThrows(DissolutionNotLinkedToTransactionException.class,
                    () -> dissolutionService.initiateDissolution(command));
        }

        @Test
        void when_request_signatories_are_invalid_then_exception_thrown() {
            when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER))
                    .thenReturn(of(aDissolution().withId(DISSOLUTION_ID).withTransactionId(TRANSACTION_ID).build()));
            when(companyOfficerService.getActiveDirectorsForCompany(COMPANY_NUMBER)).thenReturn(Map.of(OFFICER_ID, aCompanyOfficer().build()));
            when(companyOfficerService.areSelectedDirectorsValid(any(), any())).thenReturn(of("Invalid signatories"));

            final var command = aDissolutionInitiationCommand().withTransaction(transaction).build();

            assertThrows(DissolutionInvalidSignatoriesException.class,
                    () -> dissolutionService.initiateDissolution(command));
        }

        @Test
        void assigns_the_signatories_to_the_dissolution() {
            when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER))
                    .thenReturn(of(aDissolution().withId(DISSOLUTION_ID).withTransactionId(TRANSACTION_ID).build()));
            when(companyOfficerService.getActiveDirectorsForCompany(COMPANY_NUMBER)).thenReturn(Map.of(
                    "officer-id-1", aCompanyOfficer().withOfficerId("officer-id-1").withName("John Doe").build(),
                    "officer-id-2", aCompanyOfficer().withOfficerId("officer-id-2").withName("Jane Smith").build()));
            when(companyOfficerService.areSelectedDirectorsValid(any(), any())).thenReturn(empty());

            dissolutionService.initiateDissolution(aDissolutionInitiationCommand()
                    .withTransaction(transaction)
                    .withSignatories(
                            aDirectorRequest().withOfficerId("officer-id-1"),
                            aDirectorRequest().withOfficerId("officer-id-2"))
                    .build());

            verify(repository).save(dissolutionCaptor.capture());

            assertThat(dissolutionCaptor.getValue().getData().getDirectors())
                    .extracting(DissolutionDirector::getOfficerId, DissolutionDirector::getName)
                    .containsExactlyInAnyOrder(
                            tuple("officer-id-1", "John Doe"),
                            tuple("officer-id-2", "Jane Smith"));
        }

        @Test
        void sends_notifications_to_signatories() {
            final Dissolution draftDissolution = aDissolution().withId(DISSOLUTION_ID).withTransactionId(TRANSACTION_ID).withStatus(DRAFT).build();

            when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER)).thenReturn(of(draftDissolution));
            when(companyOfficerService.getActiveDirectorsForCompany(COMPANY_NUMBER)).thenReturn(Map.of("officer-id-1", aCompanyOfficer().withOfficerId("officer-id-1").build()));
            when(companyOfficerService.areSelectedDirectorsValid(any(), any())).thenReturn(empty());

            dissolutionService.initiateDissolution(aDissolutionInitiationCommand()
                    .withTransaction(transaction)
                    .withSignatories(aDirectorRequest().withOfficerId("officer-id-1"))
                    .build());

            verify(emailService).notifySignatoriesToSign(draftDissolution);
        }

        @Test
        void changes_the_dissolution_status_to_PENDING() {
            when(repository.findDraftDissolutionForUserAndCompany(USER_ID, COMPANY_NUMBER))
                    .thenReturn(of(aDissolution().withId(DISSOLUTION_ID).withTransactionId(TRANSACTION_ID).withStatus(DRAFT).build()));
            when(companyOfficerService.getActiveDirectorsForCompany(COMPANY_NUMBER)).thenReturn(Map.of("officer-id-1", aCompanyOfficer().withOfficerId("officer-id-1").build()));
            when(companyOfficerService.areSelectedDirectorsValid(any(), any())).thenReturn(empty());

            dissolutionService.initiateDissolution(aDissolutionInitiationCommand()
                    .withTransaction(transaction)
                    .withSignatories(aDirectorRequest().withOfficerId("officer-id-1"))
                    .build());

            verify(repository).save(dissolutionCaptor.capture());

            assertThat(dissolutionCaptor.getValue().getStatus()).isEqualTo(PENDING);
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class findAndUpdateSignatory {

        private Dissolution dissolution;
        private UpdateSignatoryDetailsCommand command;
        private Transaction transaction;

        @BeforeEach
        void initialize() {
            final DissolutionDirectorPatchRequest body = aDissolutionDirectorPatchRequest().build();

            final var createdBy = generateCreatedBy();
            createdBy.setUserId(USER_ID);
            createdBy.setEmail(EMAIL);

            dissolution = aDissolution()
                    .withId(DISSOLUTION_ID)
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withDirectors(aDissolutionDirector().withOfficerId(OFFICER_ID))
                    .withCreatedBy(createdBy)
                    .build();

            transaction = aTransaction()
                    .withStatus(TransactionStatus.OPEN)
                    .withCompanyNumber(COMPANY_NUMBER)
                    .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                    .build();

            command = new UpdateSignatoryDetailsCommand(transaction, COMPANY_NUMBER, USER_ID, OFFICER_ID, body.getEmail(), body.getOnBehalfName());
        }

        @Test
        void when_pending_dissolution_exists_then_signatory_details_are_updated() {
            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

            dissolutionService.findAndUpdateSignatory(command);

            verify(patcher, times(1)).updateSignatory(dissolution, command);
        }

        @Test
        void when_pending_dissolution_does_not_exist_then_dissolution_not_found_exception_thrown() {
            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> dissolutionService.findAndUpdateSignatory(command))
                    .isInstanceOf(DissolutionNotFoundException.class)
                    .hasMessage("Pending Dissolution not found for company number " + COMPANY_NUMBER);

            verify(patcher, never()).updateSignatory(any(), any());
        }

        @Test
        void when_transaction_is_closed_then_invalid_transaction_state_exception_thrown() {
            transaction.setStatus(TransactionStatus.CLOSED);

            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

            assertThatThrownBy(() -> dissolutionService.findAndUpdateSignatory(command))
                    .isInstanceOf(InvalidTransactionStateException.class)
                    .hasMessage("Transaction status CLOSED does not match expected status OPEN");

            verify(patcher, never()).updateSignatory(any(), any());
        }

        @Test
        void when_transaction_is_not_linked_to_company_then_invalid_transaction_state_exception_thrown() {
            final DissolutionDirectorPatchRequest body = aDissolutionDirectorPatchRequest().build();
            transaction = aTransaction()
                    .withStatus(TransactionStatus.OPEN)
                    .withCompanyNumber("87654321") // Different company number
                    .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                    .build();
            command = new UpdateSignatoryDetailsCommand(transaction, COMPANY_NUMBER, USER_ID, OFFICER_ID, body.getEmail(), body.getOnBehalfName());

            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

            assertThatThrownBy(() -> dissolutionService.findAndUpdateSignatory(command))
                    .isInstanceOf(InvalidTransactionStateException.class)
                    .hasMessage("Transaction does not belong to company " + COMPANY_NUMBER);

            verify(patcher, never()).updateSignatory(any(), any());
        }

        @Test
        void when_transaction_is_not_linked_to_dissolution_then_dissolution_not_linked_to_transaction_exception_thrown() {
            final DissolutionDirectorPatchRequest body = aDissolutionDirectorPatchRequest().build();
            transaction = aTransaction()
                    .withStatus(TransactionStatus.OPEN)
                    .withCompanyNumber(COMPANY_NUMBER)
                    .build();

            command = new UpdateSignatoryDetailsCommand(transaction, COMPANY_NUMBER, USER_ID, OFFICER_ID, body.getEmail(), body.getOnBehalfName());
            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

            assertThatThrownBy(() -> dissolutionService.findAndUpdateSignatory(command))
                    .isInstanceOf(DissolutionNotLinkedToTransactionException.class)
                    .hasMessage("Transaction is not linked to dissolution " + DISSOLUTION_ID);

            verify(patcher, never()).updateSignatory(any(), any());
        }

        @Test
        void when_non_applicant_attempts_to_update_signatory_details_then_dissolution_update_signatory_exception_thrown() {
            final DissolutionDirectorPatchRequest body = aDissolutionDirectorPatchRequest().build();
            command = new UpdateSignatoryDetailsCommand(transaction, COMPANY_NUMBER, OFFICER_ID, OFFICER_ID, body.getEmail(), body.getOnBehalfName());
            when(repository.findPendingDissolutionByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

            assertThatThrownBy(() -> dissolutionService.findAndUpdateSignatory(command))
                    .isInstanceOf(DissolutionUpdateSignatoryException.class)
                    .hasMessage("Only the applicant can update signatory");

            verify(patcher, never()).updateSignatory(any(), any());
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
    class NotifySignatoryToSign {

        @Test
        void sends_signing_notification_to_signatory() {

            var companyNumber = "123456789";
            var dissolutionId = "dissolution-id-1";
            var signatoryId = "signatory-id-1";
            var signatoryEmail = "signatory@test.co.uk";

            final Dissolution pendingDissolution = aDissolution()
                    .withId(dissolutionId)
                    .withCompanyNumber(companyNumber)
                    .withDirectors(aDissolutionDirector().withOfficerId(signatoryId).withEmail(signatoryEmail)).build();

            when(repository.findPendingDissolutionByCompanyNumber(companyNumber)).thenReturn(of(pendingDissolution));

            var transaction = aTransaction()
                    .withCompanyNumber(companyNumber)
                    .withStatus(OPEN)
                    .withResources(generateTransactionResource(FILING_KIND_DS01, dissolutionId))
                    .build();

            var command = new ResendSignatoryNotificationCommand(transaction, companyNumber, signatoryId);

            dissolutionService.resendSignatoryNotification(command);

            verify(emailService, times(1)).notifySignatoryToSign(pendingDissolution, signatoryEmail);
        }

        @Test
        void when_no_PENDING_dissolution_exists_for_provided_company_number_then_exception_thrown() {

            when(repository.findPendingDissolutionByCompanyNumber(any())).thenReturn(empty());

            var command = new ResendSignatoryNotificationCommand(aTransaction().withCompanyNumber("123456").build(), "123456", "789");

            AssertionsForClassTypes.assertThatThrownBy(() -> dissolutionService.resendSignatoryNotification(command)).isInstanceOf(DissolutionNotFoundException.class);
        }

        @Test
        void when_no_signatory_found_for_signatory_id_then_exception_thrown() {

            when(repository.findPendingDissolutionByCompanyNumber(any())).thenReturn(of(aDissolution()
                    .withId("a-dissolution-id")
                    .withDirectors(aDissolutionDirector().withOfficerId("a-signatory-id")).build()));

            var transaction = aTransaction()
                    .withCompanyNumber("123456")
                    .withStatus(OPEN)
                    .withResources(generateTransactionResource(FILING_KIND_DS01, "a-dissolution-id"))
                    .build();

            var command = new ResendSignatoryNotificationCommand(transaction, "123456", "a-different-signatory-id");

            AssertionsForClassTypes.assertThatThrownBy(() -> dissolutionService.resendSignatoryNotification(command)).isInstanceOf(DissolutionSignatoryNotFoundException.class);
        }

        @ParameterizedTest(name = "{index} => {0}")
        @MethodSource("invalidTransactions")
        void when_transaction_validation_fails_then_exception_thrown(String scenario, Transaction transaction, Class<? extends RuntimeException> expectedException, String expectedMessage) {

            when(repository.findPendingDissolutionByCompanyNumber(any())).thenReturn(of(aDissolution().withId("a-dissolution-id").withCompanyNumber("123456").build()));

            var command = new ResendSignatoryNotificationCommand(transaction, "123456", "789");

            AssertionsForClassTypes.assertThatThrownBy(() -> dissolutionService.resendSignatoryNotification(command)).isInstanceOf(expectedException).hasMessage(expectedMessage);
        }

        static Stream<Arguments> invalidTransactions() {
            return Stream.of(
                    Arguments.of("company number does not match", aTransaction()
                                    .withCompanyNumber("654321")
                                    .withStatus(OPEN)
                                    .withResources(generateTransactionResource(FILING_KIND_DS01, "a-dissolution-id")).build(),
                            InvalidTransactionStateException.class,
                            "Transaction does not belong to company 123456"
                    ),
                    Arguments.of("status is not OPEN", aTransaction()
                                    .withStatus(CLOSED)
                                    .withCompanyNumber("654321")
                                    .withResources(generateTransactionResource(FILING_KIND_DS01, "a-dissolution-id")).build(),
                            InvalidTransactionStateException.class,
                            "Transaction status CLOSED does not match expected status OPEN"
                    ),
                    Arguments.of("transaction is not linked to the dissolution", aTransaction()
                                    .withResources(generateTransactionResource(FILING_KIND_DS01, "a-invalid-dissolution-id"))
                                    .withCompanyNumber("123456")
                                    .withStatus(OPEN).build(),
                            DissolutionNotLinkedToTransactionException.class,
                            "Transaction is not linked to dissolution a-dissolution-id"
                    )
            );
        }
    }
}
