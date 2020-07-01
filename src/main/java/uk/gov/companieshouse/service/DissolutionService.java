package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.DissolutionGetResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Optional;

@Service
public class DissolutionService {

    private final DissolutionCreator creator;
    private final DissolutionGetter getter;
    private final DissolutionRepository repository;

    @Autowired
    public DissolutionService(DissolutionCreator creator, DissolutionGetter getter, DissolutionRepository repository) {
        this.creator = creator;
        this.getter = getter;
        this.repository = repository;
    }

    public DissolutionCreateResponse create(DissolutionCreateRequest body, String companyNumber, String userId, String ip, String email) {
        return creator.create(body, companyNumber, userId, ip, email);
    }

    public boolean doesDissolutionRequestExistForCompany(String companyNumber) {
        return repository.findByCompanyNumber(companyNumber).isPresent();
    }

    public Optional<DissolutionGetResponse> get(String companyNumber) {
        return getter.get(companyNumber);
    }
}
