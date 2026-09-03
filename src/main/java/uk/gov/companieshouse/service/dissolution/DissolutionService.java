package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.DissolutionInvalidSignatoriesException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionSignatoryNotFoundException;
import uk.gov.companieshouse.exception.DissolutionUpdateSignatoryException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.FilingKindMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.domain.CreateDraftDissolutionCommand;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalCommand;
import uk.gov.companieshouse.model.domain.DissolutionInitiationCommand;
import uk.gov.companieshouse.model.domain.ResendSignatoryNotificationCommand;
import uk.gov.companieshouse.model.domain.UpdateSignatoryDetailsCommand;
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
import uk.gov.companieshouse.service.CompanyOfficerService;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.validator.TransactionValidator;
import uk.gov.companieshouse.service.payment.PaymentService;
import uk.gov.companieshouse.service.transaction.TransactionFiling;

import java.util.Map;
import java.util.Optional;

import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.OPEN;
import static uk.gov.companieshouse.model.enums.DissolutionStatus.PENDING;
import static uk.gov.companieshouse.util.DateTimeGenerator.generateCurrentDateTime;
import static uk.gov.companieshouse.util.DissolutionApplicantUtil.isApplicant;

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
    private final CompanyOfficerService companyOfficerService;
    private final DissolutionRequestMapper dissolutionRequestMapper;
    private final DissolutionEmailService emailService;
    private final FilingKindMapper filingKindMapper;

    @Autowired
    public DissolutionService(DissolutionCreator creator, DissolutionGetter getter, DissolutionPatcher patcher, DissolutionRepository repository, PaymentService paymentService, TransactionService transactionService, DissolutionResponseMapper responseMapper, Logger logger, CompanyOfficerService companyOfficerService, DissolutionRequestMapper dissolutionRequestMapper, DissolutionEmailService emailService, FilingKindMapper filingKindMapper) {
        this.creator = creator;
        this.getter = getter;
        this.patcher = patcher;
        this.repository = repository;
        this.paymentService = paymentService;
        this.transactionService = transactionService;
        this.responseMapper = responseMapper;
        this.logger = logger;
        this.companyOfficerService = companyOfficerService;
        this.dissolutionRequestMapper = dissolutionRequestMapper;
        this.emailService = emailService;
        this.filingKindMapper = filingKindMapper;
    }

    public DissolutionCreateResponse create(DissolutionCreateRequest body, CompanyProfile companyProfile, Map<String, CompanyOfficer> directors, String userId, String ip, String email) {
        return creator.create(body, companyProfile, directors, userId, ip, email);
    }

    public DissolutionPatchResponse addDirectorApproval(String companyNumber, DissolutionDirectorApprovalCommand command) {
        final Dissolution dissolution = repository.findByCompanyNumber(companyNumber)
                .orElseThrow(() -> new DissolutionNotFoundException(String.format("Dissolution Request not found for company number %s", companyNumber)));
        return patcher.addDirectorApproval(dissolution, command);
    }

    public void addDirectorApproval(String companyNumber, Transaction transaction, DissolutionDirectorApprovalCommand command) {
        final Dissolution dissolution = getPendingDissolution(companyNumber);

        TransactionValidator.of(transaction).hasStatus(OPEN).forCompany(companyNumber).isLinkedToDissolution(dissolution.getId()).validate();

        patcher.addDirectorApproval(dissolution, command);
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

    public Dissolution getPendingDissolution(String companyNumber) {
        return repository.findPendingDissolutionByCompanyNumber(companyNumber)
                .orElseThrow(() -> new DissolutionNotFoundException(String.format("Pending Dissolution not found for company number %s", companyNumber)));
    }

    public DissolutionCreateDraftResponse createDraft(CreateDraftDissolutionCommand command) {
        if (repository.findDraftDissolutionForUserAndCompany(command.userId(), command.companyProfile().getCompanyNumber()).isPresent()) {
            throw new ConflictException("Draft dissolution already exists for user " + command.userId());
        }

        TransactionValidator.of(command.transaction()).hasStatus(OPEN).forCompany(command.companyProfile().getCompanyNumber()).validate();

        final Dissolution dissolution = creator.createDraft(command);
        repository.insert(dissolution);

        final var kind = filingKindMapper.mapApplicationTypeToFilingKind(dissolution.getApplicationType());

        try {
            final var filing = new TransactionFiling(dissolution.getId(), kind, dissolution.getCompany().getName());
            transactionService.updateTransaction(command.transaction(), filing);
        } catch (RuntimeException e) {
            // rollback so the client can create a draft again
            logger.error(String.format("Failed to update transaction %s for dissolution %s, rolling back insert of draft dissolution", command.transaction().getId(), dissolution.getId()), e);
            repository.deleteById(dissolution.getId());
            throw e;
        }
        return responseMapper.mapToDissolutionCreateDraftResponse(command.transaction(), dissolution);
    }

    public void initiateDissolution(DissolutionInitiationCommand command) {
        final var dissolution = findDraftDissolutionOrThrow(command.companyNumber(), command.userId());
        final var activeDirectors = companyOfficerService.getActiveDirectorsForCompany(command.companyNumber());

        validateInitiateDissolution(command, dissolution.getId(), activeDirectors);

        final var signatories = dissolutionRequestMapper.mapToDissolutionDirectors(command.signatories(), activeDirectors);

        dissolution.assignSignatories(signatories);
        dissolution.changeStatus(PENDING, generateCurrentDateTime());

        // Not atomic - but this is the current approach in this code base
        // if the email fails to send, the dissolution will still be saved with PENDING status. To be addressed in a future release.
        repository.save(dissolution);
        emailService.notifySignatoriesToSign(dissolution);
    }

    private void validateInitiateDissolution(DissolutionInitiationCommand command, String dissolutionId, Map<String, CompanyOfficer> activeDirectors) {
        TransactionValidator.of(command.transaction()).hasStatus(OPEN).forCompany(command.companyNumber()).isLinkedToDissolution(dissolutionId).validate();

        if (repository.findPendingDissolutionByCompanyNumber(command.companyNumber()).isPresent()) {
            throw new ConflictException(String.format("Pending dissolution already exists for company number %s", command.companyNumber()));
        }

        companyOfficerService
                .areSelectedDirectorsValid(activeDirectors, command.signatories())
                .ifPresent(error -> {
                    throw new DissolutionInvalidSignatoriesException(error);
                });
    }

    private Dissolution findDraftDissolutionOrThrow(String companyNumber, String userId) {
        return repository.findDraftDissolutionForUserAndCompany(userId, companyNumber)
                .orElseThrow(() -> new DissolutionNotFoundException(String.format("Draft dissolution not found for user %s and company number %s.", userId, companyNumber)));
    }

    public void findAndUpdateSignatory(UpdateSignatoryDetailsCommand command) {
        final var dissolution = getPendingDissolution(command.companyNumber());

        TransactionValidator.of(command.transaction())
                .hasStatus(TransactionStatus.OPEN)
                .forCompany(command.companyNumber())
                .isLinkedToDissolution(dissolution.getId())
                .validate();

        if (!isApplicant(command.userId(), dissolution)) {
            throw new DissolutionUpdateSignatoryException("Only the applicant can update signatory");
        }

        patcher.updateSignatory(dissolution, command);
    }

    public void resendSignatoryNotification(ResendSignatoryNotificationCommand command) {
        final Dissolution dissolution = getPendingDissolution(command.companyNumber());

        TransactionValidator.of(command.transaction())
                .hasStatus(TransactionStatus.OPEN)
                .forCompany(command.companyNumber())
                .isLinkedToDissolution(dissolution.getId())
                .validate();

        final var signatoryEmail = dissolution.findSignatory(command.signatoryId())
                .map(DissolutionDirector::getEmail)
                .orElseThrow(() -> new DissolutionSignatoryNotFoundException(
                        "No signatory found for signatory id " + command.signatoryId()));

        emailService.notifySignatoryToSign(dissolution, signatoryEmail);
    }
}
