package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.CompanyType;

public class CompanyProfileFixtures {
    public static CompanyProfile generateCompanyProfile() {
        return new CompanyProfile.Builder()
                .withCompanyName("My Company")
                .withType(CompanyType.LTD.getValue())
                .withCompanyNumber("10001")
                .withCompanyStatus(CompanyStatus.ACTIVE.getValue())
                .build();
    }
}
