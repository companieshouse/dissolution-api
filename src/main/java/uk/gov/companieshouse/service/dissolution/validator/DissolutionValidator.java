package uk.gov.companieshouse.service.dissolution.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
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
    private final Logger logger;

    @Autowired
    public DissolutionValidator(CompanyProfileService companyProfileService, CompanyOfficerService companyOfficerService, Logger logger) {
        this.companyProfileService = companyProfileService;
        this.companyOfficerService = companyOfficerService;
        this.logger = logger;
    }

    public Optional<String> checkBusinessRules(CompanyProfile companyProfile, Map<String, CompanyOfficer> companyDirectors, List<DirectorRequest> selectedDirectors) {
        if (!companyProfileService.isCompanyClosable(companyProfile)) {
            logger.info(ERROR_COMPANY_NOT_CLOSABLE);
            return Optional.of(ERROR_COMPANY_NOT_CLOSABLE);
        }

        logger.info("Are selected directors valid: " + companyOfficerService.areSelectedDirectorsValid(companyDirectors, selectedDirectors));

        return companyOfficerService.areSelectedDirectorsValid(companyDirectors, selectedDirectors);
    }
}
