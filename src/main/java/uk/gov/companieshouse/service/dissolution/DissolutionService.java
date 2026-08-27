package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalData;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateDraftResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.DissolutionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.validator.TransactionValidator;
import uk.gov.companieshouse.service.payment.PaymentService;

import java.util.Map;
import java.util.Optional;

@Service
public class DissolutionService {

    private final DissolutionCreator creator;
    private final DissolutionGetter getter;
    private final DissolutionPatcher patcher;
    private final DissolutionRepository repository;
    private final PaymentService paymentService;
    private final TransactionService transactionService;
    private final DissolutionResponseMapper responseMapper;
    private final Logger logger;

    @Autowired
    public DissolutionService(DissolutionCreator creator, DissolutionGetter getter, DissolutionPatcher patcher, DissolutionRepository repository, PaymentService paymentService, TransactionService transactionService, DissolutionResponseMapper responseMapper, Logger logger) {
        this.creator = creator;
        this.getter = getter;
        this.patcher = patcher;
        this.repository = repository;
        this.paymentService = paymentService;
        this.transactionService = transactionService;
        this.responseMapper = responseMapper;
        this.logger = logger;
    }

    public DissolutionCreateResponse create(DissolutionCreateRequest body, CompanyProfile companyProfile, Map<String, CompanyOfficer> directors, String userId, String ip, String email) {
        return creator.create(body, companyProfile, directors, userId, ip, email);
    }

    public DissolutionPatchResponse addDirectorApproval(String companyNumber, DissolutionDirectorApprovalData directorApprovalData) {
        final Dissolution dissolution = repository.findByCompanyNumber(companyNumber)
                .orElseThrow(() -> new DissolutionNotFoundException(String.format("Dissolution Request not found for company number %s", companyNumber)));
        return patcher.addDirectorApproval(dissolution, directorApprovalData);
    }

    public void addDirectorApproval(String companyNumber, Transaction transaction, DissolutionDirectorApprovalData directorApprovalData) {
        final Dissolution dissolution = repository.findPendingDissolutionByCompanyNumber(companyNumber)
                .orElseThrow(() -> new DissolutionNotFoundException(String.format("Pending Dissolution not found for company number %s", companyNumber)));

        TransactionValidator.of(transaction).hasStatus(TransactionStatus.OPEN).forCompany(companyNumber).isLinkedToDissolution(dissolution.getId()).validate();

        patcher.addDirectorApproval(dissolution, directorApprovalData);
    }

    public void handlePayment(PaymentPatchRequest body, String applicationReference) throws DissolutionNotFoundException {
        patcher.handlePayment(body, applicationReference);
    }

    public void setPaymentReference(String paymentReference, String applicationReference) throws DissolutionNotFoundException {
        patcher.setPaymentReference(paymentReference, applicationReference);
    }

    public boolean doesDissolutionRequestExistForCompanyByCompanyNumber(String companyNumber) {
        return repository.findByCompanyNumber(companyNumber).isPresent();
    }

    public boolean doesDissolutionRequestExistForCompanyByApplicationReference(String applicationReference) {
        return repository.findByDataApplicationReference(applicationReference).isPresent();
    }

    /**
     * Resolves the dissolution application for the given company, falling back in order across
     * a submitted dissolution, a pending (transaction-model) dissolution, a submitted dissolution
     * with no verdict yet reached, and a draft dissolution for the given user. If found, the
     * payment status is reconciled before being returned.
     */
    public Optional<DissolutionGetResponse> resolveDissolutionApplication(String userId, String companyNumber) {
        var dissolutionDto = findActiveDissolution(companyNumber)
                .or(() -> findPendingDissolution(companyNumber))
                .or(() -> findSubmittedDissolutionWithNoVerdict(companyNumber))
                .or(() -> findDraftDissolution(userId, companyNumber));

        dissolutionDto.ifPresent(this::reconcilePaymentStatus);

        return dissolutionDto;
    }

    public Optional<DissolutionGetResponse> findActiveDissolution(String companyNumber) {
        return getter.getByCompanyNumber(companyNumber);
    }

    public Optional<DissolutionGetResponse> getByApplicationReference(String applicationReference) {
        return getter.getByApplicationReference(applicationReference);
    }

    public Optional<DissolutionGetResponse> findPendingDissolution(String companyNumber) {
        return getter.getPendingDissolution(companyNumber);
    }

    public Optional<DissolutionGetResponse> findSubmittedDissolutionWithNoVerdict(String companyNumber) {
        return repository.findFirstByCompanyNumberAndStatusOrderBySubmittedAtDesc(companyNumber, DissolutionStatus.SUBMITTED)
                .filter(dissolution -> !transactionService.hasVerdictBeenReached(dissolution.getTransactionId()))
                .map(responseMapper::mapToDissolutionGetResponse);
    }

    public Optional<DissolutionGetResponse> findDraftDissolution(String userId, String companyNumber) {
        return getter.getDraftDissolution(userId, companyNumber);
    }

    // This logic was moved verbatim from DissolutionController as part of a refactor and is not newly authored here.
    // It is considered legacy/sub-optimal and is planned for removal/rework in an upcoming release.
    private void reconcilePaymentStatus(DissolutionGetResponse dissolutionGetResponse) {
        String paymentRef = dissolutionGetResponse.getPaymentReference();

        if (paymentRef == null || paymentRef.isEmpty() || !dissolutionGetResponse.getApplicationStatus().equals(ApplicationStatus.PENDING_PAYMENT)) {
            return;
        }

        // payment could be complete, we need to get up-to-date status to be sure
        String paymentStatus = paymentService.getPaymentStatus(paymentRef);

        if (paymentStatus == null) {
            logger.info(String.format("Error getting payment status for paymentRef: [%s], resetting payment ref", paymentRef));

            // error retrieving payment status, so reset payment reference to allow user to restart payment
            try {
                setPaymentReference("", dissolutionGetResponse.getApplicationReference());
            } catch (DissolutionNotFoundException e) {
                throw new NotFoundException();
            }
        } else if (paymentStatus.equals("accepted")) {
            dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PAID);
        }
    }

    public Dissolution getDissolutionById(String dissolutionId) {
        return repository.findById(dissolutionId).orElseThrow(() -> new DissolutionNotFoundException("No dissolution found with id " + dissolutionId));
    }

    public DissolutionCreateDraftResponse createDraft(Transaction transaction, CompanyProfile companyProfile, String userId, String ip, String email) {
        if (repository.findDraftDissolutionForUserAndCompany(userId, companyProfile.getCompanyNumber()).isPresent()) {
            throw new ConflictException("Draft dissolution already exists for user " + userId);
        }

        TransactionValidator.of(transaction).hasStatus(TransactionStatus.OPEN).forCompany(companyProfile.getCompanyNumber()).validate();

        return creator.createDraft(transaction, companyProfile, userId, ip, email);
    }
}
