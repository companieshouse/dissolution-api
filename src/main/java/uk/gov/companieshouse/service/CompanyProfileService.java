package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.CompanyProfileClient;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.mapper.CompanyProfileMapper;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.service.dissolution.validator.CompanyClosableValidator;

@Service
public class CompanyProfileService {
    private final CompanyClosableValidator validator;
    private final CompanyProfileClient companyProfileClient;
    private final CompanyProfileMapper companyProfileMapper;

    @Autowired
    public CompanyProfileService(CompanyClosableValidator validator, CompanyProfileClient companyProfileClient, CompanyProfileMapper companyProfileMapper) {
        this.validator = validator;
        this.companyProfileClient = companyProfileClient;
        this.companyProfileMapper = companyProfileMapper;
    }

    public boolean isCompanyClosable(CompanyProfile company) {
        return this.validator.isCompanyClosable(company);
    }

    public CompanyProfile getCompanyProfile(String companyNumber, String passThroughTokenHeader) {
        return companyProfileClient.getCompanyProfile(companyNumber, passThroughTokenHeader)
                .map(companyProfileMapper::mapToCompanyProfile)
                .orElseThrow(() -> new NotFoundException("Company profile not found for company number " + companyNumber));
    }
}
