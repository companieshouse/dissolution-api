package uk.gov.companieshouse.service.dissolution.director;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Optional;

@Service
public class DissolutionDirectorService {

    public static final String DIRECTOR_IS_NOT_PENDING_APPROVAL = "Director is not pending approval";
    public static final String ONLY_THE_APPLICANT_CAN_UPDATE_SIGNATORY = "Only the applicant can update signatory";
    private final DissolutionDirectorGetter getter;
    private final DissolutionDirectorPatcher patcher;
    private final DissolutionRepository repository;

    @Autowired
    public DissolutionDirectorService(DissolutionDirectorGetter getter, DissolutionDirectorPatcher patcher, DissolutionRepository repository) {
        this.getter = getter;
        this.patcher = patcher;
        this.repository = repository;
    }

    public DissolutionDirectorPatchResponse updateSignatory(String companyNumber, DissolutionDirectorPatchRequest body, String directorId) throws DissolutionNotFoundException {
        return patcher.updateSignatory(companyNumber, body, directorId);
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
