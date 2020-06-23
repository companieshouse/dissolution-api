package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.CreateDissolutionRequestDTO;
import uk.gov.companieshouse.model.dto.CreateDissolutionResponseDTO;
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

    public CreateDissolutionResponseDTO create(CreateDissolutionRequestDTO body, String companyNumber, String userId, String ip, String email) {
        return creator.create(body, companyNumber, userId, ip, email);
    }

    public boolean doesDissolutionRequestExistForCompany(String companyNumber) {
        return repository.findByCompanyNumber(companyNumber).isPresent();
    }
}
