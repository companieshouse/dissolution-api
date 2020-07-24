package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.exception.ServiceException;

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

    public Optional<String> checkBusinessRules(CompanyProfileApi companyProfileApi) {
        if (!companyProfileService.isCompanyClosable(companyProfileApi)) {
            return Optional.of("Company must be of a closable type and have an active status");
        }

        if (!companyOfficerService.hasEnoughOfficersSelected(companyProfileApi.getCompanyNumber())) {
            return Optional.of("A majority of directors must be selected");
        }

        return Optional.empty();
    }
}
