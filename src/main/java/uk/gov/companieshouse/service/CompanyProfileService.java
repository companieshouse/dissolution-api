package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.CompanyProfile;

@Service
public class CompanyProfileService {
    private final CompanyClosableValidator validator;

    @Autowired
    public CompanyProfileService(CompanyClosableValidator validator) {
        this.validator = validator;
    }

    public boolean isCompanyClosable(CompanyProfile company) {
        return this.validator.isCompanyClosable(company);
    }
}
