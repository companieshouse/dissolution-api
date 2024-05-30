package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.CompanyType;

public class CompanyProfileApiFixtures {
    public static CompanyProfileApi generateCompanyProfileApi() {
        final CompanyProfileApi companyProfile = new CompanyProfileApi();

        companyProfile.setCompanyName("My Company");
        companyProfile.setType(CompanyType.LTD.getValue());
        companyProfile.setCompanyNumber("10001");
        companyProfile.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        return companyProfile;
    }
}
