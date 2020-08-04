package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.repository.DissolutionRepository;

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

    public DissolutionCreateResponse create(DissolutionCreateRequest body, CompanyProfile companyProfile, String userId, String ip, String email) {
        return creator.create(body, companyProfile, userId, ip, email);
    }

    public DissolutionPatchResponse addDirectorApproval(String companyNumber, String userId, String ip, String email) {
        return patcher.addDirectorApproval(companyNumber, userId, ip, email);
    }

    public void updatePaymentStatus(PaymentPatchRequest data, String companyNumber) {
        patcher.updatePaymentInformation(data.getPaymentReference(), data.getPaidAt(), companyNumber);
    }

    public boolean doesDissolutionRequestExistForCompany(String companyNumber) {
        return repository.findByCompanyNumber(companyNumber).isPresent();
    }

    public Optional<DissolutionGetResponse> get(String companyNumber) {
        return getter.get(companyNumber);
    }

    public boolean isDirectorPendingApproval(String companyNumber, String userEmail) {
        return getter.isDirectorPendingApproval(companyNumber, userEmail);
    }
}
