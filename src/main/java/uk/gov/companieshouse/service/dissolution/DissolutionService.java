package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
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
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.validator.TransactionValidator;

import java.util.Map;
import java.util.Optional;

@Service
public class DissolutionService {

    private final DissolutionCreator creator;
    private final DissolutionGetter getter;
    private final DissolutionPatcher patcher;
    private final DissolutionRepository repository;

    @Autowired
    public DissolutionService(DissolutionCreator creator, DissolutionGetter getter, DissolutionPatcher patcher, DissolutionRepository repository) {
        this.creator = creator;
        this.getter = getter;
        this.patcher = patcher;
        this.repository = repository;
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
                .orElseThrow(() -> new DissolutionNotFoundException(String.format("Dissolution Request not found for company number %s", companyNumber)));

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

    public Optional<DissolutionGetResponse> getByCompanyNumber(String companyNumber) {
        return getter.getByCompanyNumber(companyNumber);
    }

    public Optional<DissolutionGetResponse> getByApplicationReference(String applicationReference) {
        return getter.getByApplicationReference(applicationReference);
    }

    public Optional<DissolutionGetResponse> getPendingDissolution(String companyNumber) {
        return getter.getPendingDissolution(companyNumber);
    }

    public Optional<DissolutionGetResponse> getDraftDissolution(String userId, String companyNumber) {
        return getter.getDraftDissolution(userId, companyNumber);
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
