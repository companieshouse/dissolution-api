package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.CompanyOfficersClient;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.List;

@Service
public class CompanyOfficerService {
    private final CompanyOfficersClient companyOfficersClient;
    private final CompanyOfficerValidator companyOfficerValidator;
    private final DissolutionRepository repository;

    @Autowired
    public CompanyOfficerService(
            CompanyOfficersClient companyOfficersClient,
            DissolutionRepository repository,
            CompanyOfficerValidator companyOfficerValidator
    ) {
        this.companyOfficersClient = companyOfficersClient;
        this.repository = repository;
        this.companyOfficerValidator = companyOfficerValidator;
    }

    public boolean hasEnoughOfficersSelected(String companyNumber) {
        final List<DissolutionDirector> selectedDirectors = repository.findByCompanyNumber(companyNumber)
                .get()
                .getData()
                .getDirectors();
        final List<CompanyOfficerApi> companyOfficers = companyOfficersClient.getCompanyOfficers(companyNumber);
        return companyOfficerValidator.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);
    }
}
