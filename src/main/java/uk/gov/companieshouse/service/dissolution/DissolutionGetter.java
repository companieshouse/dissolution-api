package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

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

    public Optional<DissolutionGetResponse> getByCompanyNumber(String companyNumber) {
        return repository
                .findByCompanyNumber(companyNumber)
                .map(responseMapper::mapToDissolutionGetResponse);
    }

    public boolean isDirectorPendingApproval(String companyNumber, String officerId) {
        return repository.findByCompanyNumber(companyNumber)
                .map(dissolution -> isDirectorPendingApprovalForDissolution(officerId, dissolution))
                .orElse(false);
    }

    private boolean isDirectorPendingApprovalForDissolution(String officerId, Dissolution dissolution) {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .anyMatch(director -> director.getOfficerId().equals(officerId) && !director.hasDirectorApproval());
    }
}
