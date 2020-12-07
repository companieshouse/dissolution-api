package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class DissolutionService {

    public static final String DIRECTOR_IS_NOT_PENDING_APPROVAL = "Director is not pending approval";
    public static final String ONLY_THE_APPLICANT_CAN_UPDATE_SIGNATORY = "Only the applicant can update signatory";
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

    public DissolutionPatchResponse addDirectorApproval(String companyNumber, String userId, DissolutionPatchRequest body) throws DissolutionNotFoundException {
        return patcher.addDirectorApproval(companyNumber, userId, body);
    }

    public DissolutionDirectorPatchResponse updateSignatory(String companyNumber, DissolutionDirectorPatchRequest body, String directorId) throws DissolutionNotFoundException{
        return patcher.updateSignatory(companyNumber, body, directorId);
    }

    public void handlePayment(PaymentPatchRequest body, String applicationReference) throws DissolutionNotFoundException {
        patcher.handlePayment(body, applicationReference);
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

    public boolean doesDirectorExist(String companyNumber, String officerId) {
        return getter.doesDirectorExist(companyNumber, officerId);
    }

    public Optional<String> checkPatchDirectorConstraints(String companyNumber, String directorId, String email) throws DissolutionNotFoundException {
        final Dissolution dissolution = repository.findByCompanyNumber(companyNumber).orElseThrow(DissolutionNotFoundException::new);
        if (!getter.isDirectorPendingApprovalForDissolution(directorId, dissolution)) {
            return Optional.of(DIRECTOR_IS_NOT_PENDING_APPROVAL);
        }
        if (!getter.doesEmailBelongToApplicant(email, dissolution)) {
            return Optional.of(ONLY_THE_APPLICANT_CAN_UPDATE_SIGNATORY);
        }
        return Optional.empty();
    }
}
