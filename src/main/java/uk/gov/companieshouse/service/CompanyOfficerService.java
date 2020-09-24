package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.CompanyOfficersClient;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.enums.OfficerRole;
import uk.gov.companieshouse.service.dissolution.validator.CompanyOfficerValidator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompanyOfficerService {

    private static final List<String> OFFICER_ROLES = Arrays.asList(
            OfficerRole.DIRECTOR.getValue(),
            OfficerRole.LLP_MEMBER.getValue()
    );

    private final CompanyOfficersClient client;
    private final CompanyOfficerValidator validator;

    @Autowired
    public CompanyOfficerService(CompanyOfficersClient client, CompanyOfficerValidator validator) {
        this.client = client;
        this.validator = validator;
    }

    public Map<String, CompanyOfficer> getActiveDirectorsForCompany(String companyNumber) {
        return client
                .getCompanyOfficers(companyNumber)
                .stream()
                .filter(this::isActiveDirector)
                .collect(Collectors.toMap(this::getOfficerId, Function.identity()));
    }

    private boolean isActiveDirector(CompanyOfficer officer) {
        return officer.getResignedOn() == null && OFFICER_ROLES.contains(officer.getOfficerRole());
    }

    private String getOfficerId(CompanyOfficer officer) {
        return officer.getLinks().getOfficer().getAppointments().split("/")[2];
    }

    public Optional<String> areSelectedDirectorsValid(Map<String, CompanyOfficer> companyDirectors, List<DirectorRequest> selectedDirectors) {
        return validator.areSelectedDirectorsValid(companyDirectors, selectedDirectors);
    }
}
