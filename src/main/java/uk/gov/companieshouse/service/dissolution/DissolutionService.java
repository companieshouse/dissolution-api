package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.DissolutionStatus;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.payment.PaymentService;
import uk.gov.companieshouse.util.TransactionHelper;

import java.util.Map;
import java.util.Optional;

@Service
public class DissolutionService {

    private final DissolutionCreator creator;
    private final DissolutionGetter getter;
    private final DissolutionPatcher patcher;
    private final DissolutionRepository repository;
    private final TransactionHelper transactionHelper;
    private final PaymentService paymentService;
    private final TransactionService transactionService;
    private final DissolutionResponseMapper responseMapper;
    private final Logger logger;

    @Autowired
    public DissolutionService(DissolutionCreator creator, DissolutionGetter getter, DissolutionPatcher patcher, DissolutionRepository repository, TransactionHelper transactionHelper, PaymentService paymentService, TransactionService transactionService, DissolutionResponseMapper responseMapper, Logger logger) {
        this.creator = creator;
        this.getter = getter;
        this.patcher = patcher;
        this.repository = repository;
        this.transactionHelper = transactionHelper;
        this.paymentService = paymentService;
        this.transactionService = transactionService;
        this.responseMapper = responseMapper;
        this.logger = logger;
    }

    public DissolutionCreateResponse create(DissolutionCreateRequest body, CompanyProfile companyProfile, Map<String, CompanyOfficer> directors, String userId, String ip, String email) {
        return creator.create(body, companyProfile, directors, userId, ip, email);
    }

    public DissolutionPatchResponse addDirectorApproval(String companyNumber, String userId, DissolutionPatchRequest body) throws DissolutionNotFoundException {
        return patcher.addDirectorApproval(companyNumber, userId, body);
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
     * a submitted dissolution, a pending (transaction-model) dissolution, a processed dissolution
     * with no verdict yet reached, and a draft dissolution for the given user. If found, the
     * payment status is reconciled before being returned.
     */
    public Optional<DissolutionGetResponse> resolveDissolutionApplication(String userId, String companyNumber, String passThroughTokenHeader) {
        var dissolutionDto = getByCompanyNumber(companyNumber)
                .or(() -> getPendingDissolution(companyNumber))
                .or(() -> getProcessedDissolutionWithNoVerdict(companyNumber, passThroughTokenHeader))
                .or(() -> getDraftDissolution(userId, companyNumber));

        dissolutionDto.ifPresent(this::reconcilePaymentStatus);

        return dissolutionDto;
    }

    public Optional<DissolutionGetResponse> getByCompanyNumber(String companyNumber) {
        return getter.getByCompanyNumber(companyNumber);
    }

    public Optional<DissolutionGetResponse> getByApplicationReference(String applicationReference) {
        return getter.getByApplicationReference(applicationReference);
    }

    public boolean isDirectorPendingApproval(String companyNumber, String officerId) {
        return getter.isDirectorPendingApproval(companyNumber, officerId);
    }

    public Optional<DissolutionGetResponse> getPendingDissolution(String companyNumber) {
        return getter.getPendingDissolution(companyNumber);
    }

    public Optional<DissolutionGetResponse> getProcessedDissolutionWithNoVerdict(String companyNumber, String passThroughTokenHeader) {
        return repository.findFirstByCompanyNumberAndStatusOrderByProcessedAtDesc(companyNumber, DissolutionStatus.PROCESSED)
                .filter(dissolution -> !transactionService.hasVerdictBeenReached(dissolution.getTransactionId(), passThroughTokenHeader))
                .map(responseMapper::mapToDissolutionGetResponse);
    }

    public Optional<DissolutionGetResponse> getDraftDissolution(String userId, String companyNumber) {
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

    public Optional<Dissolution> getDissolutionById(String dissolutionId) {
        return repository.findById(dissolutionId);
    }

    public Dissolution getDissolutionForTransaction(Transaction transaction, String dissolutionId) throws DissolutionNotLinkedToTransactionException, DissolutionNotFoundException {
        if (!transactionHelper.isTransactionLinkedToDissolution(transaction, dissolutionId)) {
            throw new DissolutionNotLinkedToTransactionException("Transaction not linked to dissolution");
        }
        return getDissolutionById(dissolutionId).orElseThrow(() -> new DissolutionNotFoundException("No dissolution found with id " + dissolutionId));
    }
}
