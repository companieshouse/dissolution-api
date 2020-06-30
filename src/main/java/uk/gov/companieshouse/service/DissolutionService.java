package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.DissolutionGetResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

@Service
public class DissolutionService {

    private final DissolutionCreator creator;
    private final DissolutionRepository repository;

    @Autowired
    public DissolutionService(DissolutionCreator creator, DissolutionRepository repository) {
        this.creator = creator;
        this.repository = repository;
    }

    public DissolutionCreateResponse create(DissolutionCreateRequest body, String companyNumber, String userId, String ip, String email) {
        return creator.create(body, companyNumber, userId, ip, email);
    }

    public boolean doesDissolutionRequestExistForCompany(String companyNumber) {
        return repository.findByCompanyNumber(companyNumber).isPresent();
    }

    public DissolutionGetResponse get(String companyNumber) {
        return creator.get(companyNumber);
    }
}
