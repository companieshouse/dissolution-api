package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.repository.DissolutionRepository;

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

    public DissolutionPatchResponse addDirectorApproval(String companyNumber, String userId, String ip, String officerId) {
        return patcher.addDirectorApproval(companyNumber, userId, ip, officerId);
    }

    public void handlePayment(PaymentPatchRequest data, String companyNumber) {
        patcher.handlePayment(data.getPaymentReference(), data.getPaidAt(), companyNumber);
    }

    public boolean doesDissolutionRequestExistForCompany(String companyNumber) {
        return repository.findByCompanyNumber(companyNumber).isPresent();
    }

    public Optional<DissolutionGetResponse> getByCompanyNumber(String companyNumber) {
        return getter.getByCompanyNumber(companyNumber);
    }

    public Optional<Dissolution> getByApplicationReference(String applicationReference) {
        return repository.findByDataApplicationReference(applicationReference);
    }

    public boolean isDirectorPendingApproval(String companyNumber, String officerId) {
        return getter.isDirectorPendingApproval(companyNumber, officerId);
    }
}
