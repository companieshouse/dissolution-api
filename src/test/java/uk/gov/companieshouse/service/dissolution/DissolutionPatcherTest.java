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
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionSubmissionMapper;
import uk.gov.companieshouse.mapper.PaymentInformationMapper;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateGenerator;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentInformation;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentPatchRequest;

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

    private static final String COMPANY_NUMBER = "12345678";
    private static final String APPLICATION_REFERENCE = "ABC123";
    private static final String USER_ID = "1234";
    private static final String OFFICER_ID = "abc123";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String OFFICER_ID_TWO = "def456";

    private Dissolution dissolution;
    private DissolutionPatchResponse response;
    private DirectorApproval approval;
    private DissolutionCertificate certificate;
    private ArgumentCaptor<Dissolution> dissolutionCaptor;

    @BeforeEach
    void init() {
        dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getData().getDirectors().get(0).setOfficerId(OFFICER_ID);
        response = DissolutionFixtures.generateDissolutionPatchResponse();
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
    void patch_updatesStatusToPendingPayment_ifAllDirectorHaveApprovedForMultiDirectorCompany() throws DissolutionNotFoundException {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);

        final List<DissolutionDirector> directors = DissolutionFixtures.generateDissolutionDirectorList();
        directors.get(0).setOfficerId(OFFICER_ID);
        directors.get(1).setOfficerId(OFFICER_ID_TWO);
        directors.get(1).setDirectorApproval(approval);
        dissolution.getData().setDirectors(directors);

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
    void patch_updatesStatusToPendingPayment_ifAllDirectorHaveApprovedForSingleDirectorCompany() throws DissolutionNotFoundException {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);

        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(java.util.Optional.of(dissolution));
        when(responseMapper.mapToDissolutionPatchResponse(dissolution)).thenReturn(response);
        when(approvalMapper.mapToDirectorApproval(USER_ID, IP_ADDRESS)).thenReturn(approval);

        final DissolutionPatchResponse result = patcher.addDirectorApproval(COMPANY_NUMBER, USER_ID, body);

        verify(responseMapper).mapToDissolutionPatchResponse(dissolution);
        verify(repository).save(dissolutionCaptor.capture());
        verifyNoInteractions(dissolutionEmailService);

        assertEquals(response, result);
        assertEquals(
                ApplicationStatus.PENDING_PAYMENT,
                dissolutionCaptor.getValue().getData().getApplication().getStatus()
        );
    }

    /**@Test
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
    }**/

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
}