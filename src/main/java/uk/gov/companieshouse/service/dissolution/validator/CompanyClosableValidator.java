package uk.gov.companieshouse.service.dissolution.validator;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.enums.CompanyOverseasPrefix;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.CompanyType;

import java.util.Arrays;
import java.util.List;


@Service
public class CompanyClosableValidator {
    private static final List<String> CLOSABLE_TYPES = Arrays.asList(
            CompanyType.LTD.getValue(),
            CompanyType.PLC.getValue(),
            CompanyType.LLP.getValue(),
            CompanyType.PRIVATE_UNLIMITED.getValue(),
            CompanyType.OLD_PUBLIC_COMPANY.getValue(),
            CompanyType.PRIVATE_LIMITED_GUARANT_NSC_LIMITED_EXEMPTION.getValue(),
            CompanyType.PRIVATE_LIMITED_GUARANT_NSC.getValue(),
            CompanyType.PRIVATE_UNLIMITED_NSC.getValue(),
            CompanyType.PRIVATE_LIMITED_SHARES_SECTION_30_EXEMPTION.getValue(),
            CompanyType.NORTHERN_IRELAND.getValue(),
            CompanyType.NORTHERN_IRELAND_OTHER.getValue()
    );

    private static final List<String> OVERSEAS_PREFIXES = Arrays.asList(
            CompanyOverseasPrefix.FC.getValue(),
            CompanyOverseasPrefix.SF.getValue(),
            CompanyOverseasPrefix.NF.getValue()
    );

    public boolean isCompanyClosable(CompanyProfile company) {
        return isCompanyTypeClosable(company.getType())
                && isCompanyActive(company.getCompanyStatus())
                && !isCompanyOverseas(company.getCompanyNumber());
    }

    private boolean isCompanyTypeClosable(String type) {
        return CLOSABLE_TYPES.contains(type);
    }

    private boolean isCompanyActive(String status) {
        return status.equals(CompanyStatus.ACTIVE.getValue());
    }

    private boolean isCompanyOverseas(String companyNumber) {
        return OVERSEAS_PREFIXES.contains(companyNumber.substring(0, 2));
    }
}
