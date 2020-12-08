package uk.gov.companieshouse.service.dissolution.director;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.repository.DissolutionRepository;

@Service
public class DissolutionDirectorGetter {

    private final DissolutionRepository repository;

    @Autowired
    public DissolutionDirectorGetter(DissolutionRepository repository) {
        this.repository = repository;
    }

    public boolean doesDirectorExist(String companyNumber, String officerId) {
        return repository.findByCompanyNumber(companyNumber)
                .map(dissolution -> doesDirectorExistInDissolutionRequest(officerId, dissolution))
                .orElse(false);
    }

    private boolean doesDirectorExistInDissolutionRequest(String officerId, Dissolution dissolution) {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .anyMatch(director -> director.getOfficerId().equals(officerId));
    }
}
