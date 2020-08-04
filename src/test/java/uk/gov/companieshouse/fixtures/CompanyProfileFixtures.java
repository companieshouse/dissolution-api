package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.CompanyType;

public class CompanyProfileFixtures {
    public static CompanyProfile generateCompanyProfile() {
        final CompanyProfile companyProfile = new CompanyProfile();

        companyProfile.setCompanyName("My Company");
        companyProfile.setType(CompanyType.LTD.getValue());
        companyProfile.setCompanyNumber("10001");
        companyProfile.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        return companyProfile;
    }
}
