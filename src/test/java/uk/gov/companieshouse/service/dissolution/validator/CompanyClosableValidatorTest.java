package uk.gov.companieshouse.service.dissolution.validator;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.CompanyType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CompanyClosableValidatorTest {

    private final CompanyClosableValidator mapper = new CompanyClosableValidator();

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypeLtdAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.LTD.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypePlcAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.PLC.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypeLlpAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.LLP.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypePrivateUnlimitedAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.PRIVATE_UNLIMITED.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypeOldPublicCompanyAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.OLD_PUBLIC_COMPANY.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypePrivateLimitedGuarantNscLimitedExemptionAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.PRIVATE_LIMITED_GUARANT_NSC_LIMITED_EXEMPTION.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypePrivateLimitedGuarantNscAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.PRIVATE_LIMITED_GUARANT_NSC.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypePrivateUnlimitedNscAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.PRIVATE_UNLIMITED_NSC.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypePrivateLimitedSharesSection30ExemptionAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.PRIVATE_LIMITED_SHARES_SECTION_30_EXEMPTION.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypeNorthernIrelandAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.NORTHERN_IRELAND.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypeNorthernIrelandOtherAndIsActive_returnsTrue() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.NORTHERN_IRELAND_OTHER.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertTrue(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypeLtdAndIsActiveAndOverseas_returnsFalse() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.LTD.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());
        company.setCompanyNumber("FC123456");

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertFalse(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypePlcAndIsActiveAndOverseas_returnsFalse() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.PLC.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());
        company.setCompanyNumber("NF123456");

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertFalse(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypeLlpAndIsActiveAndOverseas_returnsFalse() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.LLP.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());
        company.setCompanyNumber("SF123456");

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertFalse(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypeLtdAndIsDissolved_returnsFalse() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.LTD.getValue());
        company.setCompanyStatus(CompanyStatus.DISSOLVED.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertFalse(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypePlcAndIsDissolved_returnsFalse() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.PLC.getValue());
        company.setCompanyStatus(CompanyStatus.DISSOLVED.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertFalse(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasTypeLlpAndIsDissolved_returnsFalse() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.LLP.getValue());
        company.setCompanyStatus(CompanyStatus.DISSOLVED.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertFalse(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasNonClosableTypeAndIsActive_returnsFalse() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.EEIG.getValue());
        company.setCompanyStatus(CompanyStatus.ACTIVE.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertFalse(isClosable);
    }

    @Test
    public void mapCompanyDetailsToClosable_companyHasNonClosableTypeAndIsDissolved_returnsFalse() {
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setType(CompanyType.EEIG.getValue());
        company.setCompanyStatus(CompanyStatus.DISSOLVED.getValue());

        final boolean isClosable = mapper.isCompanyClosable(company);

        assertFalse(isClosable);
    }
}
