package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.mapper.DirectorApprovalMapper;
import uk.gov.companieshouse.mapper.DissolutionDirectorResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionSubmissionMapper;
import uk.gov.companieshouse.mapper.PaymentInformationMapper;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorFixtures.generateDissolutionPatchDirectorRequest;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentInformation;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentPatchRequest;

@ExtendWith(MockitoExtension.class)
public class DissolutionPatcherTest {

    @InjectMocks
    private DissolutionPatcher patcher;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionResponseMapper responseMapper;

    @Mock
    private DissolutionDirectorResponseMapper directorResponseMapper;

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

    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPLICATION_REFERENCE = "ABC123";
    private static final String USER_ID = "1234";
    private static final String OFFICER_ID = "abc123";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String OFFICER_ID_TWO = "def456";
    private static final String EMAIL = "mail@mail.com";
    private static final String ON_BEHALF_NAME = "on behalf name";

    private Dissolution dissolution;
    private DissolutionPatchResponse response;
    private DissolutionDirectorPatchResponse directorResponse;
    private DirectorApproval approval;
    private DissolutionCertificate certificate;
    private ArgumentCaptor<Dissolution> dissolutionCaptor;

    @BeforeEach
    void init() {
        dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getData().getDirectors().get(0).setOfficerId(OFFICER_ID);
        response = DissolutionFixtures.generateDissolutionPatchResponse();
        directorResponse = DissolutionFixtures.generateDissolutionDirectorPatchResponse();
        approval = DissolutionFixtures.generateDirectorApproval();
        certificate = generateDissolutionCertificate();
        dissolutionCaptor = ArgumentCaptor.forClass(Dissolution.class);
    }

    @Test
    void patch_addsApprovalToSingleDirector_savesInDatabase() throws DissolutionNotFoundException {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(java.util.Optional.of(dissolution));
        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        patcher.addDirectorApproval(COMPANY_NUMBER, USER_ID, body);

        verify(repository).save(dissolutionCaptor.capture());

        assertSame(dissolutionCaptor.getValue().getData().getDirectors().get(0).getDirectorApproval(), approval);
    }

    @Test
    void patch_updatesStatusToPendingPayment_ifAllDirectorHaveApproved() throws DissolutionNotFoundException {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(java.util.Optional.of(dissolution));
        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        final DissolutionPatchResponse result = patcher.addDirectorApproval(COMPANY_NUMBER, USER_ID, body);

        verify(responseMapper).mapToDissolutionPatchResponse(dissolution);
        verify(repository).save(dissolutionCaptor.capture());
        verify(dissolutionEmailService).sendPendingPaymentEmail(dissolutionCaptor.capture());

        assertEquals(response, result);
        assertEquals(
                ApplicationStatus.PENDING_PAYMENT,
                dissolutionCaptor.getValue().getData().getApplication().getStatus()
        );
    }

    @Test
    void patch_generatesCertificateAndSavesInDatabase_ifAllDirectorHaveApproved() throws DissolutionNotFoundException {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(java.util.Optional.of(dissolution));
        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);
        when(certificateGenerator.generateDissolutionCertificate(dissolution)).thenReturn(certificate);

        patcher.addDirectorApproval(COMPANY_NUMBER, USER_ID, body);

        verify(certificateGenerator).generateDissolutionCertificate(dissolution);
        verify(repository).save(dissolutionCaptor.capture());

        assertEquals(certificate, dissolutionCaptor.getValue().getCertificate());
    }

    @Test
    void patch_doesNotUpdateStatus_ifNotAllDirectorHaveApproved() throws DissolutionNotFoundException {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);

        final List<DissolutionDirector> directors = DissolutionFixtures.generateDissolutionDirectorList();
        directors.get(0).setOfficerId(OFFICER_ID);
        directors.get(1).setOfficerId(OFFICER_ID_TWO);
        dissolution.getData().setDirectors(directors);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(java.util.Optional.of(dissolution));
        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        patcher.addDirectorApproval(COMPANY_NUMBER, USER_ID, body);

        verify(repository).save(dissolutionCaptor.capture());

        assertEquals(
                ApplicationStatus.PENDING_APPROVAL,
                dissolutionCaptor.getValue().getData().getApplication().getStatus()
        );
    }

    @Test
    void patch_doesNotGenerateCertificate_ifNotAllDirectorHaveApproved() throws DissolutionNotFoundException {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);

        final DissolutionDirector directorOne = DissolutionFixtures.generateDissolutionDirector();
        directorOne.setOfficerId(OFFICER_ID);
        directorOne.setDirectorApproval(null);

        final DissolutionDirector directorTwo = DissolutionFixtures.generateDissolutionDirector();
        directorTwo.setOfficerId(OFFICER_ID_TWO);
        directorTwo.setDirectorApproval(null);

        dissolution.getData().setDirectors(Arrays.asList(directorOne, directorTwo));

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(java.util.Optional.of(dissolution));
        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        patcher.addDirectorApproval(COMPANY_NUMBER, USER_ID, body);

        verify(certificateGenerator, never()).generateDissolutionCertificate(dissolution);
        verify(repository).save(dissolutionCaptor.capture());

        assertNull(dissolutionCaptor.getValue().getCertificate());
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

    @Test
    void patch_updateSignatory_updatesSignatoryWithEmail_SavesInDatabaseAndSendsEmail() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();

        body.setEmail(EMAIL);
        body.setOnBehalfName(null);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID);

        verify(dissolutionEmailService).notifySignatoryToSign(dissolutionCaptor.capture(), eq(EMAIL));
        verify(repository).save(dissolutionCaptor.capture());

        assertEquals(EMAIL, dissolutionCaptor.getValue().getData().getDirectors().get(0).getEmail());
        assertNull(null, dissolutionCaptor.getValue().getData().getDirectors().get(0).getOnBehalfName());
    }

    @Test
    void patch_updateSignatory_updatesSignatoryWithEmailAndOnBehalfName_SavesInDatabaseAndSendsEmail() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        dissolution.getData().getDirectors().get(0).setEmail(EMAIL+"asd");

        body.setEmail(EMAIL);
        body.setOnBehalfName(ON_BEHALF_NAME);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID);

        verify(dissolutionEmailService).notifySignatoryToSign(dissolutionCaptor.capture(), eq(EMAIL));
        verify(repository).save(dissolutionCaptor.capture());

        assertEquals(EMAIL, dissolutionCaptor.getValue().getData().getDirectors().get(0).getEmail());
        assertEquals(ON_BEHALF_NAME, dissolutionCaptor.getValue().getData().getDirectors().get(0).getOnBehalfName());
    }

    @Test
    void patch_updateSignatory_updatesSignatoryWithTheSameEmailButDifferentName_SavesInDatabaseAndSendsEmail() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        dissolution.getData().getDirectors().get(0).setEmail(EMAIL);
        dissolution.getData().getDirectors().get(0).setOnBehalfName(null);

        body.setEmail(EMAIL);
        body.setOnBehalfName(ON_BEHALF_NAME);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));
        when(directorResponseMapper.mapToDissolutionDirectorPatchResponse(dissolution)).thenReturn(directorResponse);

        final DissolutionDirectorPatchResponse result = patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID);

        verify(directorResponseMapper).mapToDissolutionDirectorPatchResponse(dissolution);
        verify(dissolutionEmailService).notifySignatoryToSign(dissolutionCaptor.capture(), eq(EMAIL));
        verify(repository).save(dissolutionCaptor.capture());

        assertEquals(directorResponse, result);
        assertEquals(EMAIL, dissolutionCaptor.getValue().getData().getDirectors().get(0).getEmail());
        assertEquals(ON_BEHALF_NAME, dissolutionCaptor.getValue().getData().getDirectors().get(0).getOnBehalfName());
    }

    @Test
    void patch_updateSignatory_updatesSignatoryWithTheSameEmailAndOnBehalfName_DoesNotSaveInDatabaseAndDoesNotSendEmail() throws DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        dissolution.getData().getDirectors().get(0).setEmail(EMAIL);
        dissolution.getData().getDirectors().get(0).setOnBehalfName(ON_BEHALF_NAME);

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
        dissolution.getData().getDirectors().get(0).setEmail(EMAIL);
        dissolution.getData().getDirectors().get(0).setOnBehalfName(null);

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
        DissolutionDirector director = generateDissolutionDirector();
        director.setOfficerId("random");
        dissolution.getData().setDirectors(Collections.singletonList(director));

        body.setEmail(EMAIL);
        body.setOnBehalfName(null);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(dissolution));

        DissolutionNotFoundException exception = assertThrows(DissolutionNotFoundException.class, () -> {
        patcher.updateSignatory(COMPANY_NUMBER, body, OFFICER_ID);
        });

        verify(dissolutionEmailService, times(0)).notifySignatoryToSign(any(), any());
        verify(repository, times(0)).save(any());

        assertNotNull(exception);
    }
}