package uk.gov.companieshouse.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.CompanyType;

import java.util.Arrays;
import java.util.List;

@Service
public class CompanyClosableValidator {
    private static final List<String> CLOSABLE_TYPES = Arrays.asList(
            CompanyType.LTD.getValue(),
            CompanyType.PLC.getValue()
    );

    public boolean isCompanyClosable(CompanyProfile company) {
        return isCompanyTypeClosable(company.getType()) && isCompanyActive(company.getCompanyStatus());
    }

    private boolean isCompanyTypeClosable(String type) {
        return CLOSABLE_TYPES.contains(type);
    }

    private boolean isCompanyActive(String status) {
        return status.equals(CompanyStatus.ACTIVE.getValue());
    }
}
