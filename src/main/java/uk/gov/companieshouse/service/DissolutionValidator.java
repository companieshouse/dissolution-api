package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

import java.util.List;
import java.util.Optional;

@Service
public class DissolutionValidator {

    private final CompanyProfileService companyProfileService;
    private final CompanyOfficerService companyOfficerService;

    @Autowired
    public DissolutionValidator(CompanyProfileService companyProfileService, CompanyOfficerService companyOfficerService) {
        this.companyProfileService = companyProfileService;
        this.companyOfficerService = companyOfficerService;
    }

    public Optional<String> checkBusinessRules(CompanyProfile companyProfile, List<DirectorRequest> selectedDirectors) {
        if (!companyProfileService.isCompanyClosable(companyProfile)) {
            return Optional.of("Company must be of a closable type, have an active status and must not be an overseas company");
        }

        if (!companyOfficerService.hasEnoughOfficersSelected(companyProfile.getCompanyNumber(), selectedDirectors)) {
            return Optional.of("A majority of directors must be selected");
        }

        return Optional.empty();
    }
}
