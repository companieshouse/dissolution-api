package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class CompanyOfficerFixtures {
    public static List<CompanyOfficerApi> generateCompanyOfficerList() {
        final CompanyOfficerApi officerOne = new CompanyOfficerApi();

        officerOne.setName("John Doe");
        officerOne.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());

        final CompanyOfficerApi officerTwo = new CompanyOfficerApi();

        officerTwo.setName("Fred Mercure");
        officerTwo.setAppointedOn(LocalDateTime.now().minusWeeks(1).toLocalDate());

        return Arrays.asList(officerOne, officerTwo);
    }
}
