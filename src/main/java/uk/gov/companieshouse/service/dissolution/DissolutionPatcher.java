package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.mapper.DirectorApprovalMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionSubmissionMapper;
import uk.gov.companieshouse.mapper.PaymentInformationMapper;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateGenerator;

import java.sql.Timestamp;

@Service
public class DissolutionPatcher {

    private final DissolutionRepository repository;
    private final DissolutionResponseMapper responseMapper;
    private final DirectorApprovalMapper approvalMapper;
    private final PaymentInformationMapper paymentInformationMapper;
    private final DissolutionSubmissionMapper dissolutionSubmissionMapper;
    private final DissolutionCertificateGenerator certificateGenerator;
    private final DissolutionEmailService dissolutionEmailService;

    @Autowired
    public DissolutionPatcher(
            DissolutionRepository repository,
            DissolutionResponseMapper responseMapper,
            DirectorApprovalMapper approvalMapper,
            PaymentInformationMapper paymentInformationMapper,
            DissolutionSubmissionMapper dissolutionSubmissionMapper,
            DissolutionCertificateGenerator certificateGenerator,
            DissolutionEmailService dissolutionEmailService
    ) {
        this.repository = repository;
        this.responseMapper = responseMapper;
        this.approvalMapper = approvalMapper;
        this.paymentInformationMapper = paymentInformationMapper;
        this.dissolutionSubmissionMapper = dissolutionSubmissionMapper;
        this.certificateGenerator = certificateGenerator;
        this.dissolutionEmailService = dissolutionEmailService;
    }

    public DissolutionPatchResponse addDirectorApproval(String companyNumber, String userId, String ip, String officerId) {
        final Dissolution dissolution = this.repository.findByCompanyNumber(companyNumber).orElseThrow(NotFoundException::new);

        this.addDirectorApproval(userId, ip, officerId, dissolution);

        if (!this.hasDirectorsLeftToApprove(dissolution)) {
            handleFinalApproval(dissolution);
        }

        this.repository.save(dissolution);

        return this.responseMapper.mapToDissolutionPatchResponse(companyNumber);
    }

    private void handleFinalApproval(Dissolution dissolution) {
        setDissolutionStatus(dissolution, ApplicationStatus.PENDING_PAYMENT);
        dissolution.setCertificate(this.certificateGenerator.generateDissolutionCertificate(dissolution));
        dissolutionEmailService.sendPendingPaymentEmail(dissolution);
    }

    public void handlePayment(String paymentReference, Timestamp paidAt, String companyNumber) {
        final Dissolution dissolution = this.repository.findByCompanyNumber(companyNumber).orElseThrow(NotFoundException::new);

        this.addPaymentInformation(paymentReference, paidAt, dissolution);

        setDissolutionStatus(dissolution, ApplicationStatus.PAID);

        dissolution.setSubmission(this.dissolutionSubmissionMapper.generateSubmissionInformation());

        this.repository.save(dissolution);

        dissolutionEmailService.sendSuccessfulPaymentEmail(dissolution);
    }

    private DissolutionDirector findDirector(String officerId, Dissolution dissolution) {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .filter(director -> director.getOfficerId().equals(officerId))
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    private void addPaymentInformation(String paymentReference, Timestamp paidAt, Dissolution dissolution) {
        final PaymentInformation information = paymentInformationMapper.mapToPaymentInformation(PaymentMethod.CREDIT_CARD, paymentReference, paidAt);

        dissolution.setPaymentInformation(information);
    }

    private void addDirectorApproval(String userId, String ip, String officerId, Dissolution dissolution) {
        final DirectorApproval approval = approvalMapper.mapToDirectorApproval(userId, ip);
        DissolutionDirector director = this.findDirector(officerId, dissolution);
        director.setDirectorApproval(approval);
    }

    private boolean hasDirectorsLeftToApprove(Dissolution dissolution) {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .anyMatch(director -> director.getDirectorApproval() == null);
    }

    private void setDissolutionStatus(Dissolution dissolution, ApplicationStatus status) {
        dissolution
                .getData()
                .getApplication()
                .setStatus(status);
    }
}
