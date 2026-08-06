package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.client.CompanyProfileClient;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.service.dissolution.validator.CompanyClosableValidator;

@Service
public class CompanyProfileService {
    private final CompanyClosableValidator validator;
    private final CompanyProfileClient companyProfileClient;

    @Autowired
    public CompanyProfileService(CompanyClosableValidator validator, CompanyProfileClient companyProfileClient) {
        this.validator = validator;
        this.companyProfileClient = companyProfileClient;
    }

    public boolean isCompanyClosable(CompanyProfile company) {
        return this.validator.isCompanyClosable(company);
    }

    public CompanyProfile getCompanyProfile(String companyNumber, String passThroughTokenHeader) {
        final CompanyProfileApi companyProfileApi = companyProfileClient.getCompanyProfile(companyNumber, passThroughTokenHeader).orElseThrow(
                () -> new NotFoundException("Company profile not found for company number " + companyNumber));

        return new CompanyProfile.Builder()
                .withCompanyName(companyProfileApi.getCompanyName())
                .withType(companyProfileApi.getType())
                .withCompanyNumber(companyProfileApi.getCompanyNumber())
                .withCompanyStatus(companyProfileApi.getCompanyStatus())
                .build();
    }
}
