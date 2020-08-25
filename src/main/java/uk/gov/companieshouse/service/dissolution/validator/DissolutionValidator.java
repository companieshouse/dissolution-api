package uk.gov.companieshouse.service.dissolution.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.service.CompanyOfficerService;
import uk.gov.companieshouse.service.CompanyProfileService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DissolutionValidator {

    private static final String ERROR_COMPANY_NOT_CLOSABLE = "Company must be of a closable type, have an active status and must not be an overseas company";

    private final CompanyProfileService companyProfileService;
    private final CompanyOfficerService companyOfficerService;

    @Autowired
    public DissolutionValidator(CompanyProfileService companyProfileService, CompanyOfficerService companyOfficerService) {
        this.companyProfileService = companyProfileService;
        this.companyOfficerService = companyOfficerService;
    }

    public Optional<String> checkBusinessRules(CompanyProfile companyProfile, Map<String, CompanyOfficer> companyDirectors, List<DirectorRequest> selectedDirectors) {
        if (!companyProfileService.isCompanyClosable(companyProfile)) {
            return Optional.of(ERROR_COMPANY_NOT_CLOSABLE);
        }

        return companyOfficerService.areSelectedDirectorsValid(companyDirectors, selectedDirectors);
    }
}
