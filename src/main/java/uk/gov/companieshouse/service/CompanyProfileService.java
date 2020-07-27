package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;

@Service
public class CompanyProfileService {
    private final CompanyClosableValidator companyClosableValidator;

    @Autowired
    public CompanyProfileService(CompanyClosableValidator companyClosableValidator) {
        this.companyClosableValidator = companyClosableValidator;
    }

    public boolean isCompanyClosable(CompanyProfileApi company) {
        return this.companyClosableValidator.isCompanyClosable(company);
    }
}
