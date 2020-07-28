package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.CompanyOfficersClient;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

import java.util.List;

@Service
public class CompanyOfficerService {
    private final CompanyOfficersClient companyOfficersClient;
    private final CompanyOfficerValidator companyOfficerValidator;

    @Autowired
    public CompanyOfficerService(
            CompanyOfficersClient companyOfficersClient,
            CompanyOfficerValidator companyOfficerValidator
    ) {
        this.companyOfficersClient = companyOfficersClient;
        this.companyOfficerValidator = companyOfficerValidator;
    }

    public boolean hasEnoughOfficersSelected(String companyNumber, List<DirectorRequest> selectedDirectors) {
        final List<CompanyOfficerApi> companyOfficers = companyOfficersClient.getCompanyOfficers(companyNumber);
        return companyOfficerValidator.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);
    }
}
