package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.enums.OfficerRole;

import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficerLinks;

public class CompanyOfficerTestDataBuilder {

    private String name = "John Doe";
    private String officerRole = OfficerRole.DIRECTOR.getValue();
    private String resignedOn = null;
    private String officerId = "123";

    public static CompanyOfficerTestDataBuilder aCompanyOfficer() {
        return new CompanyOfficerTestDataBuilder();
    }

    public CompanyOfficerTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CompanyOfficerTestDataBuilder withOfficerRole(String officerRole) {
        this.officerRole = officerRole;
        return this;
    }

    public CompanyOfficerTestDataBuilder withResignedOn(String resignedOn) {
        this.resignedOn = resignedOn;
        return this;
    }

    public CompanyOfficerTestDataBuilder withOfficerId(String officerId) {
        this.officerId = officerId;
        return this;
    }

    public CompanyOfficer build() {
        final CompanyOfficer officer = new CompanyOfficer();

        officer.setName(name);
        officer.setOfficerRole(officerRole);
        officer.setResignedOn(resignedOn);
        officer.setLinks(generateCompanyOfficerLinks(officerId));

        return officer;
    }
}
