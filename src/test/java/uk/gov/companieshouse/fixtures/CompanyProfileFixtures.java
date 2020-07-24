package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.CompanyType;

public class CompanyProfileFixtures {
    public static CompanyProfileApi generateCompanyProfileApi() {
        final CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyName("My Company");
        companyProfileApi.setType(CompanyType.LTD.getValue());
        companyProfileApi.setCompanyNumber("10001");
        companyProfileApi.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        return companyProfileApi;
    }
}
