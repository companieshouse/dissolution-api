package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.exception.DissolutionDirectorApprovalException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.mapper.DirectorApprovalMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionSubmissionMapper;
import uk.gov.companieshouse.mapper.PaymentInformationMapper;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalData;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.DissolutionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateGenerator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionPatchRequest;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionSubmission;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentInformation;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentPatchRequest;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;

@ExtendWith(MockitoExtension.class)
class DissolutionPatcherTest {

    @InjectMocks
    private DissolutionPatcher patcher;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionResponseMapper responseMapper;

    @Mock
    private DirectorApprovalMapper approvalMapper;

    @Mock
    private PaymentInformationMapper paymentInformationMapper;

    @Mock
    private DissolutionSubmissionMapper dissolutionSubmissionMapper;

    @Mock
    private DissolutionCertificateGenerator certificateGenerator;

    @Mock
    private DissolutionEmailService dissolutionEmailService;

    private static final String APPLICATION_REFERENCE = "ABC123";
    private static final String USER_ID = "1234";
    private static final String OFFICER_ID = "abc123";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String OFFICER_ID_TWO = "def456";
    private static final String EMAIL = "director@email.com";
    private static final String PRESENTER_EMAIL = "presenter@email.com";

    private Dissolution dissolution;
    private DissolutionPatchResponse response;
    private DirectorApproval approval;
    private ArgumentCaptor<Dissolution> dissolutionCaptor;

    @BeforeEach
    void init() {
        dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getData().getDirectors().getFirst().setOfficerId(OFFICER_ID);
        response = DissolutionFixtures.generateDissolutionPatchResponse();
        approval = DissolutionFixtures.generateDirectorApproval();
        dissolutionCaptor = ArgumentCaptor.forClass(Dissolution.class);
    }

    @Test
    void patch_addsApprovalToSingleDirector_savesInDatabase() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        patcher.addDirectorApproval(dissolution, directorApprovalData);

        verify(repository).save(dissolutionCaptor.capture());

        assertSame(dissolutionCaptor.getValue().getData().getDirectors().getFirst().getDirectorApproval(), approval);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void patch_updatesStatusToPendingPayment_ifAllDirectorHaveApprovedForMultiDirectorCompany(boolean hasTransactionId) {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        final List<DissolutionDirector> directors = DissolutionFixtures.generateDissolutionDirectorList();
        directors.get(0).setOfficerId(OFFICER_ID);
        directors.get(1).setOfficerId(OFFICER_ID_TWO);
        directors.get(1).setDirectorApproval(approval);
        dissolution.getData().setDirectors(directors);

        // This is needed as the transaction model dissolution process leverages the same approval patching
        // flow, but conditionally updates the top level dissolution.status based on the presence of a transaction_id.
        // The existing process does not update the top level dissolution.status but the application.status instead.
        // This is a temporary measure until we fully migrate to the transaction model dissolution process.
        if (hasTransactionId) dissolution.setTransactionId(TRANSACTION_ID);

        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        final DissolutionPatchResponse result = patcher.addDirectorApproval(dissolution, directorApprovalData);

        verify(responseMapper).mapToDissolutionPatchResponse(dissolution);
        verify(repository).save(dissolutionCaptor.capture());
        verify(dissolutionEmailService).sendPendingPaymentEmail(dissolutionCaptor.capture());

        assertEquals(response, result);
        assertReadyForPayment(hasTransactionId, dissolutionCaptor.getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void patch_updatesStatusToPendingPayment_ifAllDirectorHaveApprovedForSingleDirectorCompanyAndPresenterIsDirector(boolean hasTransactionId) {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        final var soleDirector = aDissolutionDirector().withEmail(EMAIL);
        dissolution = aDissolution()
                .withOnlyDirector(soleDirector)
                .withCreatedByEmail(EMAIL)
                .build();

        if (hasTransactionId) dissolution.setTransactionId(TRANSACTION_ID);

        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        final DissolutionPatchResponse result = patcher.addDirectorApproval(dissolution, directorApprovalData);

        verify(responseMapper).mapToDissolutionPatchResponse(dissolution);
        verify(repository).save(dissolutionCaptor.capture());
        verifyNoInteractions(dissolutionEmailService);

        assertEquals(response, result);
        assertReadyForPayment(hasTransactionId, dissolutionCaptor.getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void patch_updatesStatusToPendingPayment_ifAllDirectorHaveApprovedForSingleDirectorCompanyAndPresenterIsNotDirector(boolean hasTransactionId) {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        // set the createdBy email to be different from the director email
        final var soleDirector = aDissolutionDirector().withEmail(EMAIL);
        dissolution = aDissolution()
                .withOnlyDirector(soleDirector)
                .withCreatedByEmail(PRESENTER_EMAIL)
                .build();

        if (hasTransactionId) dissolution.setTransactionId(TRANSACTION_ID);

        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        final DissolutionPatchResponse result = patcher.addDirectorApproval(dissolution, directorApprovalData);

        verify(responseMapper).mapToDissolutionPatchResponse(dissolution);
        verify(repository).save(dissolutionCaptor.capture());
        verify(dissolutionEmailService).sendPendingPaymentEmail(dissolutionCaptor.capture());

        assertEquals(response, result);
        assertReadyForPayment(hasTransactionId, dissolutionCaptor.getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void patch_doesNotUpdateStatus_ifNotAllDirectorHaveApproved(boolean hasTransactionId) {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        final List<DissolutionDirector> directors = DissolutionFixtures.generateDissolutionDirectorList();
        directors.get(0).setOfficerId(OFFICER_ID);
        directors.get(1).setOfficerId(OFFICER_ID_TWO);
        dissolution.getData().setDirectors(directors);

        if (hasTransactionId) {
            dissolution.setTransactionId(TRANSACTION_ID);
            dissolution.changeStatus(DissolutionStatus.PENDING, LocalDateTime.now());
        }

        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        patcher.addDirectorApproval(dissolution, directorApprovalData);

        verify(repository).save(dissolutionCaptor.capture());

        assertStatusUnchanged(hasTransactionId, dissolutionCaptor.getValue());
    }

    @Test
    void patch_doesNotGenerateCertificate_ifNotAllDirectorHaveApproved() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        final DissolutionDirector directorOne = DissolutionFixtures.generateDissolutionDirector();
        directorOne.setOfficerId(OFFICER_ID);
        directorOne.setDirectorApproval(null);

        final DissolutionDirector directorTwo = DissolutionFixtures.generateDissolutionDirector();
        directorTwo.setOfficerId(OFFICER_ID_TWO);
        directorTwo.setDirectorApproval(null);

        dissolution.getData().setDirectors(Arrays.asList(directorOne, directorTwo));

        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        patcher.addDirectorApproval(dissolution, directorApprovalData);

        verify(certificateGenerator, never()).generateDissolutionCertificate(dissolution);
        verify(repository).save(dissolutionCaptor.capture());

        assertNull(dissolutionCaptor.getValue().getCertificate());
    }

    @Test
    void patch_throwsDissolutionDirectorApprovalException_ifOfficerIdInRequestIsNotFound() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        dissolution = aDissolution()
                .withDirectors(aDissolutionDirector().withOfficerId(OFFICER_ID_TWO))
                .build();

        assertThrows(DissolutionDirectorApprovalException.class, () -> patcher.addDirectorApproval(dissolution, directorApprovalData));

        verify(repository, never()).save(any(Dissolution.class));
    }

    @Test
    void patch_throwsDissolutionDirectorApprovalException_ifDirectorAlreadyApproved() {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        dissolution = aDissolution()
                .withDirectors(aDissolutionDirector()
                        .withOfficerId(OFFICER_ID)
                        .withDirectorApproval(generateDirectorApproval()))
                .build();

        assertThrows(DissolutionDirectorApprovalException.class, () -> patcher.addDirectorApproval(dissolution, directorApprovalData));

        verify(repository, never()).save(any(Dissolution.class));
    }

    @Test
    void patch_updatesDissolutionWithPaymentAndSubmissionInformation_savesInDatabase() throws DissolutionNotFoundException {
        PaymentPatchRequest data = generatePaymentPatchRequest();
        PaymentInformation paymentInformation = generatePaymentInformation();
        DissolutionSubmission submission = generateDissolutionSubmission();

        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(java.util.Optional.of(dissolution));
        when(paymentInformationMapper
                .mapToPaymentInformation(data))
                .thenReturn(paymentInformation);
        when(dissolutionSubmissionMapper.generateSubmissionInformation()).thenReturn(submission);

        patcher.handlePayment(data, APPLICATION_REFERENCE);
        verify(repository).save(dissolutionCaptor.capture());
        verify(dissolutionEmailService).sendSuccessfulPaymentEmail(dissolutionCaptor.capture());

        assertEquals(paymentInformation, dissolutionCaptor.getValue().getPaymentInformation());
        assertEquals(submission, dissolutionCaptor.getValue().getSubmission());
    }

    private void assertReadyForPayment(boolean hasTransactionId, Dissolution dissolution) {
        if (hasTransactionId) {
            assertEquals(DissolutionStatus.SUBMITTED, dissolution.getStatus());
        } else {
            assertEquals(ApplicationStatus.PENDING_PAYMENT, dissolution.getData().getApplication().getStatus());
        }
    }

    private void assertStatusUnchanged(boolean hasTransactionId, Dissolution dissolution) {
        if (hasTransactionId) {
            assertEquals(DissolutionStatus.PENDING, dissolution.getStatus());
        } else {
            assertEquals(ApplicationStatus.PENDING_APPROVAL, dissolution.getData().getApplication().getStatus());
        }
    }
}
