package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.mapper.DirectorApprovalMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionSubmissionMapper;
import uk.gov.companieshouse.mapper.PaymentInformationMapper;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateGenerator;

import java.util.List;

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

    public DissolutionPatchResponse addDirectorApproval(String companyNumber, String userId, DissolutionPatchRequest body) throws DissolutionNotFoundException {
        final Dissolution dissolution = this.repository.findByCompanyNumber(companyNumber).orElseThrow(DissolutionNotFoundException::new);

        this.addDirectorApproval(userId, body, dissolution);

        if (!this.hasDirectorsLeftToApprove(dissolution)) {
            handleFinalApproval(dissolution);
        }

        this.repository.save(dissolution);

        return this.responseMapper.mapToDissolutionPatchResponse(dissolution);
    }

    private void handleFinalApproval(Dissolution dissolution) {
        final List<DissolutionDirector> directors = dissolution.getData().getDirectors();
        setDissolutionStatus(dissolution, ApplicationStatus.PENDING_PAYMENT);
        dissolution.setCertificate(this.certificateGenerator.generateDissolutionCertificate(dissolution));
        if (directors.size()>1) {
            dissolutionEmailService.sendPendingPaymentEmail(dissolution);
        }
    }

    public void handlePayment(PaymentPatchRequest body, String applicationReference) throws DissolutionNotFoundException {
        final Dissolution dissolution = this.repository.findByDataApplicationReference(applicationReference).orElseThrow(DissolutionNotFoundException::new);

        this.addPaymentInformation(body, dissolution);

        setDissolutionStatus(dissolution, ApplicationStatus.PAID);

        dissolution.setSubmission(this.dissolutionSubmissionMapper.generateSubmissionInformation());

        this.repository.save(dissolution);

        dissolutionEmailService.sendSuccessfulPaymentEmail(dissolution);
    }

    public void setPaymentReference(String paymentReference, String applicationReference) throws DissolutionNotFoundException {
        final Dissolution dissolution = this.repository.findByDataApplicationReference(applicationReference).orElseThrow(DissolutionNotFoundException::new);

        this.addPaymentReference(paymentReference, dissolution);

        this.repository.save(dissolution);
    }

    private DissolutionDirector findDirector(String officerId, Dissolution dissolution) throws DissolutionNotFoundException {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .filter(director -> director.getOfficerId().equals(officerId))
                .findFirst()
                .orElseThrow(DissolutionNotFoundException::new);
    }

    private void addPaymentInformation(PaymentPatchRequest body, Dissolution dissolution) {
        final PaymentInformation information = paymentInformationMapper.mapToPaymentInformation(body);

        dissolution.setPaymentInformation(information);
    }

    private void addPaymentReference(String paymentReference, Dissolution dissolution) {
        final PaymentInformation information = paymentInformationMapper.mapPaymentReference(paymentReference);

        dissolution.setPaymentInformation(information);
    }

    private void addDirectorApproval(String userId, DissolutionPatchRequest body, Dissolution dissolution) throws DissolutionNotFoundException {
        final DirectorApproval approval = approvalMapper.mapToDirectorApproval(userId, body.getIpAddress());
        DissolutionDirector director = this.findDirector(body.getOfficerId(), dissolution);
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
