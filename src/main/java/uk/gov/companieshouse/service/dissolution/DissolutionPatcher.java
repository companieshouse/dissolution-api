package uk.gov.companieshouse.service.dissolution;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.exception.DissolutionDirectorApprovalException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.mapper.DirectorApprovalMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionSubmissionMapper;
import uk.gov.companieshouse.mapper.PaymentInformationMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalData;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.DissolutionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.certificate.DissolutionCertificateGenerator;
import static uk.gov.companieshouse.util.DissolutionApplicantUtil.doesEmailBelongToApplicant;

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

    public DissolutionPatchResponse addDirectorApproval(final Dissolution dissolution, DissolutionDirectorApprovalData directorApprovalData) {
        DissolutionDirector director = this.getDirector(directorApprovalData.officerId(), dissolution);

        if (director.hasDirectorApproval()) {
            throw new DissolutionDirectorApprovalException(String.format("Director %s has already approved", directorApprovalData.officerId()));
        }

        director.setDirectorApproval(approvalMapper.mapToDirectorApproval(directorApprovalData.userId(), directorApprovalData.ipAddress()));

        if (!this.hasDirectorsLeftToApprove(dissolution)) {
            handleFinalApproval(dissolution);
        }

        this.repository.save(dissolution);

        return this.responseMapper.mapToDissolutionPatchResponse(dissolution);
    }

    private void handleFinalApproval(Dissolution dissolution) {
        final List<DissolutionDirector> directors = dissolution.getData().getDirectors();
        if (StringUtils.isBlank(dissolution.getTransactionId())) {
            setDissolutionStatus(dissolution, ApplicationStatus.PENDING_PAYMENT);
        } else {
            dissolution.setStatus(DissolutionStatus.PROCESSED);
        }
        dissolution.setCertificate(this.certificateGenerator.generateDissolutionCertificate(dissolution));
        boolean isSoleDirectorSelfFiling = directors.size() == 1
                && doesEmailBelongToApplicant(directors.getFirst().getEmail(), dissolution);
        if (!isSoleDirectorSelfFiling) {
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

    private DissolutionDirector getDirector(String officerId, Dissolution dissolution) {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .filter(director -> director.getOfficerId().equals(officerId))
                .findFirst()
                .orElseThrow(() -> new DissolutionDirectorApprovalException(String.format("Director %s is not a signatory", officerId)));
    }

    private void addPaymentInformation(PaymentPatchRequest body, Dissolution dissolution) {
        final PaymentInformation information = paymentInformationMapper.mapToPaymentInformation(body);

        dissolution.setPaymentInformation(information);
    }

    private void addPaymentReference(String paymentReference, Dissolution dissolution) {
        final PaymentInformation information = paymentInformationMapper.mapPaymentReference(paymentReference);

        dissolution.setPaymentInformation(information);
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
