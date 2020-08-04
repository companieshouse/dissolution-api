package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficersResponse;
import uk.gov.companieshouse.model.enums.OfficerRole;

import java.util.Collections;

public class CompanyOfficerFixtures {

    public static CompanyOfficersResponse generateCompanyOfficersResponse() {
        final CompanyOfficersResponse response = new CompanyOfficersResponse();

        response.setItems(Collections.singletonList(generateCompanyOfficer()));

        return response;
    }

    public static CompanyOfficer generateCompanyOfficer() {
        final CompanyOfficer officer = new CompanyOfficer();

        officer.setName("John Doe");
        officer.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        officer.setResignedOn(null);

        return officer;
    }
}
