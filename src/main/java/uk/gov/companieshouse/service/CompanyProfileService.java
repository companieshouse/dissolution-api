package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;

@Service
public class CompanyProfileService {
    private final CompanyClosableValidator validator;

    @Autowired
    public CompanyProfileService(CompanyClosableValidator validator) {
        this.validator = validator;
    }

    public boolean isCompanyClosable(CompanyProfileApi company) {
        return this.validator.isCompanyClosable(company);
    }
}
