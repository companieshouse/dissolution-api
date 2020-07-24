package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.CompanyOfficersClient;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
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
        final List<CompanyOfficerApi> companyOfficers = client.getCompanyOfficers(companyNumber);
        return validator.areMajorityOfCompanyOfficersSelected(companyOfficers, selectedDirectors);
    }
}
