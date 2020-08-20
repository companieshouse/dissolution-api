package uk.gov.companieshouse.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.enums.OfficerRole;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyOfficerValidator {

    public boolean areMajorityOfCompanyOfficersSelected(List<CompanyOfficer> officers, List<DirectorRequest> selectedDirectors) {
        final List<String> activeOfficers = this.mapCompanyOfficersToActiveList(officers);
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

    private List<String> mapCompanyOfficersToActiveList(List<CompanyOfficer> officers) {
        return officers
                .stream()
                .filter(officer -> officer.getResignedOn() == null)
                .filter(activeOfficer -> activeOfficer.getOfficerRole().equals(OfficerRole.DIRECTOR.getValue()) || 
                activeOfficer.getOfficerRole().equals(OfficerRole.LLP_MEMBER.getValue()))
                .map(CompanyOfficer::getName)
                .collect(Collectors.toList());
    }

    private List<String> mapDissolutionDirectorsToSelectedOfficers(List<DirectorRequest> directors) {
        return directors
                .stream()
                .map(DirectorRequest::getName)
                .collect(Collectors.toList());
    }
}
