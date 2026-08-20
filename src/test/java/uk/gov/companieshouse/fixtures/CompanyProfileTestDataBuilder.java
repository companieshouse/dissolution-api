package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.CompanyType;

public class CompanyProfileTestDataBuilder {
    private String companyName = "My Company";
    private String type = CompanyType.LTD.getValue();
    private String companyNumber = "10001";
    private String companyStatus = CompanyStatus.ACTIVE.getValue();

    public static CompanyProfileTestDataBuilder aCompany() {
        return new CompanyProfileTestDataBuilder();
    }

    public CompanyProfileTestDataBuilder withCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public CompanyProfileTestDataBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public CompanyProfileTestDataBuilder withType(CompanyType type) {
        this.type = type.getValue();
        return this;
    }

    public CompanyProfileTestDataBuilder withCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public CompanyProfileTestDataBuilder withStatus(String companyStatus) {
        this.companyStatus = companyStatus;
        return this;
    }

    public CompanyProfileTestDataBuilder withStatus(CompanyStatus companyStatus) {
        this.companyStatus = companyStatus.getValue();
        return this;
    }

    public CompanyProfile build() {
        return new CompanyProfile(companyName, type, companyNumber, companyStatus);
    }
}
