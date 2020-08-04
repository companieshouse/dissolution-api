package uk.gov.companieshouse.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.officers.OfficerRoleApi;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyOfficerValidator {
    public boolean areMajorityOfCompanyOfficersSelected(
            List<CompanyOfficerApi> companyOfficers,
            List<DirectorRequest> selectedDirectors
            ) {

        final List<String> activeOfficers = this.mapCompanyOfficersToActiveList(companyOfficers);
        final List<String> selectedOfficers = this.mapDissolutionDirectorsToSelectedOfficers(selectedDirectors);

        if (activeOfficers.isEmpty()) {
            return false;
        }

        final float officersSelected = selectedOfficers.stream().filter(activeOfficers::contains).count();

        if (officersSelected == 0) {
            return false;
        }

        return officersSelected / activeOfficers.size() > 0.5;
    }

    private List<String> mapCompanyOfficersToActiveList(List<CompanyOfficerApi> companyOfficers) {
        return companyOfficers
                .stream()
                .filter(companyOfficer -> companyOfficer.getResignedOn() == null)
                .filter(companyOfficer -> companyOfficer.getOfficerRole() == OfficerRoleApi.DIRECTOR)
                .map(companyOfficerApi -> companyOfficerApi.getName())
                .collect(Collectors.toList());
    }

    private List<String> mapDissolutionDirectorsToSelectedOfficers(List<DirectorRequest> directors) {
        return directors
                .stream()
                .map(DirectorRequest::getName)
                .collect(Collectors.toList());
    }
}
