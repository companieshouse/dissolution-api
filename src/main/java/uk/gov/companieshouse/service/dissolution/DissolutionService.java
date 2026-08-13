package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.repository.DissolutionRepository;
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

    @Autowired
    public DissolutionService(DissolutionCreator creator, DissolutionGetter getter, DissolutionPatcher patcher, DissolutionRepository repository, TransactionHelper transactionHelper) {
        this.creator = creator;
        this.getter = getter;
        this.patcher = patcher;
        this.repository = repository;
        this.transactionHelper = transactionHelper;
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

    public Optional<DissolutionGetResponse> getDraftDissolution(String userId, String companyNumber) {
        return getter.getDraftDissolution(userId, companyNumber);
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
