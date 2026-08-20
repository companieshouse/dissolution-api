package uk.gov.companieshouse.service.dissolution.validator;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.CompanyType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.fixtures.CompanyProfileTestDataBuilder.aCompany;

class CompanyClosableValidatorTest {

    private final CompanyClosableValidator validator = new CompanyClosableValidator();

    @Test
    void mapCompanyDetailsToClosable_companyHasTypeLtdAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.LTD).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypePlcAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.PLC).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypeLlpAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.LLP).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypePrivateUnlimitedAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.PRIVATE_UNLIMITED).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypeOldPublicCompanyAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.OLD_PUBLIC_COMPANY).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypePrivateLimitedGuarantNscLimitedExemptionAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.PRIVATE_LIMITED_GUARANT_NSC_LIMITED_EXEMPTION).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypePrivateLimitedGuarantNscAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.PRIVATE_LIMITED_GUARANT_NSC).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypePrivateUnlimitedNscAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.PRIVATE_UNLIMITED_NSC).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypePrivateLimitedSharesSection30ExemptionAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.PRIVATE_LIMITED_SHARES_SECTION_30_EXEMPTION).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypeNorthernIrelandAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.NORTHERN_IRELAND).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypeNorthernIrelandOtherAndIsActive_returnsTrue() {
        final CompanyProfile company = aCompany().withType(CompanyType.NORTHERN_IRELAND_OTHER).withStatus(CompanyStatus.ACTIVE).build();
        assertTrue(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypeLtdAndIsActiveAndOverseas_returnsFalse() {
        final CompanyProfile company = aCompany().withType(CompanyType.LTD).withCompanyNumber("FC123456").withStatus(CompanyStatus.ACTIVE).build();
        assertFalse(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypePlcAndIsActiveAndOverseas_returnsFalse() {
        final CompanyProfile company = aCompany().withType(CompanyType.PLC).withCompanyNumber("NF123456").withStatus(CompanyStatus.ACTIVE).build();
        assertFalse(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypeLlpAndIsActiveAndOverseas_returnsFalse() {
        final CompanyProfile company = aCompany().withType(CompanyType.LLP).withCompanyNumber("SF123456").withStatus(CompanyStatus.ACTIVE).build();
        assertFalse(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypeLtdAndIsDissolved_returnsFalse() {
        final CompanyProfile company = aCompany().withType(CompanyType.LTD).withStatus(CompanyStatus.DISSOLVED).build();
        assertFalse(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypePlcAndIsDissolved_returnsFalse() {
        final CompanyProfile company = aCompany().withType(CompanyType.PLC).withStatus(CompanyStatus.DISSOLVED).build();
        assertFalse(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasTypeLlpAndIsDissolved_returnsFalse() {
        final CompanyProfile company = aCompany().withType(CompanyType.LLP).withStatus(CompanyStatus.DISSOLVED).build();
        assertFalse(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasNonClosableTypeAndIsActive_returnsFalse() {
        final CompanyProfile company = aCompany().withType(CompanyType.EEIG).withStatus(CompanyStatus.ACTIVE).build();
        assertFalse(validator.isCompanyClosable(company));
    }

    @Test
    void mapCompanyDetailsToClosable_companyHasNonClosableTypeAndIsDissolved_returnsFalse() {
        final CompanyProfile company = aCompany().withType(CompanyType.EEIG).withStatus(CompanyStatus.DISSOLVED).build();
        assertFalse(validator.isCompanyClosable(company));
    }
}
