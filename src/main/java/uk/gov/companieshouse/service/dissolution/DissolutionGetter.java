package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DissolutionGetter {

    private final DissolutionRepository repository;
    private final DissolutionResponseMapper responseMapper;

    @Autowired
    public DissolutionGetter(
            DissolutionRepository repository,
            DissolutionResponseMapper responseMapper) {
        this.repository = repository;
        this.responseMapper = responseMapper;
    }

    public Optional<DissolutionGetResponse> get(String companyNumber) {
        return repository
                .findByCompanyNumber(companyNumber)
                .map(responseMapper::mapToDissolutionGetResponse);
    }

    public boolean isDirectorPendingApproval(String companyNumber, String userEmail) {
        return repository.findByCompanyNumber(companyNumber)
                .map(dissolution -> dissolution.getData().getDirectors())
                .map(dissolutionDirectors -> this.mapDirectorsPendingApprovalToBoolean(dissolutionDirectors, userEmail))
                .orElse(false);
    }

    private boolean mapDirectorsPendingApprovalToBoolean(List<DissolutionDirector> dissolutionDirectors, String userEmail) {
        return dissolutionDirectors.stream()
                .anyMatch(dissolutionDirector -> this.mapDirectorPendingApprovalToBoolean(dissolutionDirector, userEmail));
    }

    private boolean mapDirectorPendingApprovalToBoolean(DissolutionDirector dissolutionDirector, String userEmail) {
        return dissolutionDirector.getEmail().equals(userEmail)
                && !dissolutionDirector.hasDirectorApproval();
    }
}
