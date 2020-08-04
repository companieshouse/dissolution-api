package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.CompanyOfficersClient;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

import java.util.List;

@Service
public class CompanyOfficerService {
    private final CompanyOfficersClient client;
    private final CompanyOfficerValidator validator;

    @Autowired
    public CompanyOfficerService(CompanyOfficersClient client, CompanyOfficerValidator validator) {
        this.client = client;
        this.validator = validator;
    }

    public boolean hasEnoughOfficersSelected(String companyNumber, List<DirectorRequest> selectedDirectors) {
        final List<CompanyOfficer> companyOfficers = client.getCompanyOfficers(companyNumber);
        return validator.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);
    }
}
