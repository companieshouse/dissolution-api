package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionApplication;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.domain.DissolutionUserData;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.CompanyType;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;
import static uk.gov.companieshouse.fixtures.CompanyProfileTestDataBuilder.aCompany;

class DissolutionRequestMapperTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NAME = "ComComp";
    private static final String USER_ID = "user123";
    private static final String EMAIL = "user@mail.com";
    private static final String IP_ADDRESS = "192.168.0.1";
    private static final String REFERENCE = "ABC123";
    private static final String BARCODE = "B4RC0D3";

    private final DissolutionRequestMapper requestMapper = new DissolutionRequestMapper();

    @Test
    void mapToDissolution_setsModifiedDateTime() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final DissolutionUserData userData = DissolutionFixtures.generateDissolutionUserData();
        userData.setEmail(EMAIL);
        userData.setIpAddress(IP_ADDRESS);
        userData.setUserId(USER_ID);
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = aCompany().withCompanyNumber(COMPANY_NUMBER).withCompanyName(COMPANY_NAME).build();

        final Dissolution dissolution = requestMapper.mapToDissolution(body, company, new HashMap<>(), userData, REFERENCE, BARCODE);

        assertNotNull(dissolution.getModifiedDateTime());
    }

    @Test
    void mapToDissolution_setsApplicationData_includingDefaultStatusForDS01() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final DissolutionUserData userData = DissolutionFixtures.generateDissolutionUserData();
        userData.setEmail(EMAIL);
        userData.setIpAddress(IP_ADDRESS);
        userData.setUserId(USER_ID);
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = aCompany()
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(COMPANY_NAME)
                .withType(CompanyType.PLC)
                .build();

        final Dissolution dissolution = requestMapper.mapToDissolution(body, company, new HashMap<>(), userData, REFERENCE, BARCODE);

        assertEquals(BARCODE, dissolution.getData().getApplication().getBarcode());
        assertEquals(REFERENCE, dissolution.getData().getApplication().getReference());
        assertTrue(dissolution.getActive());
        assertEquals(ApplicationStatus.PENDING_APPROVAL, dissolution.getData().getApplication().getStatus());
        assertEquals(ApplicationType.DS01, dissolution.getData().getApplication().getType());
    }

    @Test
    void mapToDissolution_setsApplicationData_includingDefaultStatusForLLDS01() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final DissolutionUserData userData = DissolutionFixtures.generateDissolutionUserData();
        userData.setEmail(EMAIL);
        userData.setIpAddress(IP_ADDRESS);
        userData.setUserId(USER_ID);
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = aCompany()
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(COMPANY_NAME)
                .withType(CompanyType.LLP)
                .build();

        final Dissolution dissolution = requestMapper.mapToDissolution(body, company, new HashMap<>(), userData, REFERENCE, BARCODE);

        assertEquals(BARCODE, dissolution.getData().getApplication().getBarcode());
        assertEquals(REFERENCE, dissolution.getData().getApplication().getReference());
        assertTrue(dissolution.getActive());
        assertEquals(ApplicationStatus.PENDING_APPROVAL, dissolution.getData().getApplication().getStatus());
        assertEquals(ApplicationType.LLDS01, dissolution.getData().getApplication().getType());
    }

    @Test
    void mapToDissolution_setsDirectorsToSignFromRequestBody() {
        final String officerId1 = "abc123";
        final String officerId2 = "def456";

        final DissolutionUserData userData = DissolutionFixtures.generateDissolutionUserData();
        userData.setEmail(EMAIL);
        userData.setIpAddress(IP_ADDRESS);
        userData.setUserId(USER_ID);

        final CompanyOfficer companyDirector1 = generateCompanyOfficer();
        companyDirector1.setName("Director who will sign themselves");

        final CompanyOfficer companyDirector2 = generateCompanyOfficer();
        companyDirector2.setName("Director who will let someone sign on behalf of them");

        final Map<String, CompanyOfficer> companyDirectors = Map.of(
                officerId1, companyDirector1,
                officerId2, companyDirector2
        );

        final CompanyProfile company = aCompany().withCompanyNumber(COMPANY_NUMBER).withCompanyName(COMPANY_NAME).build();

        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();

        final DirectorRequest selectedDirector1 = DissolutionFixtures.generateDirectorRequest();
        selectedDirector1.setOfficerId(officerId1);
        selectedDirector1.setEmail(" DIRECTOR@mail.com");
        selectedDirector1.setOnBehalfName(null);

        final DirectorRequest selectedDirector2 = DissolutionFixtures.generateDirectorRequest();
        selectedDirector2.setOfficerId(officerId2);
        selectedDirector2.setEmail("ACCOUNTANT@mail.com ");
        selectedDirector2.setOnBehalfName("Mr Accountant");

        body.setDirectors(Arrays.asList(selectedDirector1, selectedDirector2));

        final Dissolution dissolution = requestMapper.mapToDissolution(body, company, companyDirectors, userData, REFERENCE, BARCODE);

        assertEquals(2, dissolution.getData().getDirectors().size());

        final DissolutionDirector dissolutionDirector1 = dissolution.getData().getDirectors().getFirst();
        assertEquals(officerId1, dissolutionDirector1.getOfficerId());
        assertEquals("Director who will sign themselves", dissolutionDirector1.getName());
        assertEquals("director@mail.com", dissolutionDirector1.getEmail());
        assertNull(dissolutionDirector1.getOnBehalfName());

        final DissolutionDirector dissolutionDirector2 = dissolution.getData().getDirectors().get(1);
        assertEquals(officerId2, dissolutionDirector2.getOfficerId());
        assertEquals("Director who will let someone sign on behalf of them", dissolutionDirector2.getName());
        assertEquals("accountant@mail.com", dissolutionDirector2.getEmail());
        assertEquals("Mr Accountant", dissolutionDirector2.getOnBehalfName());
    }

    @Test
    void mapToDissolution_setsCompanyInformation() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final DissolutionUserData userData = DissolutionFixtures.generateDissolutionUserData();
        userData.setEmail(EMAIL);
        userData.setIpAddress(IP_ADDRESS);
        userData.setUserId(USER_ID);
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = aCompany().withCompanyNumber(COMPANY_NUMBER).withCompanyName(COMPANY_NAME).build();

        final Dissolution dissolution = requestMapper.mapToDissolution(body, company, new HashMap<>(), userData, REFERENCE, BARCODE);

        assertEquals(COMPANY_NUMBER, dissolution.getCompany().getNumber());
        assertEquals(COMPANY_NAME, dissolution.getCompany().getName());
    }

    @Test
    void mapToDissolution_setsCreatedByInformation() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final DissolutionUserData userData = DissolutionFixtures.generateDissolutionUserData();
        userData.setEmail(EMAIL);
        userData.setIpAddress(IP_ADDRESS);
        userData.setUserId(USER_ID);
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = aCompany().withCompanyNumber(COMPANY_NUMBER).withCompanyName(COMPANY_NAME).build();

        final Dissolution dissolution = requestMapper.mapToDissolution(body, company, new HashMap<>(), userData, REFERENCE, BARCODE);

        assertEquals(USER_ID, dissolution.getCreatedBy().getUserId());
        assertEquals(EMAIL, dissolution.getCreatedBy().getEmail());
        assertEquals(IP_ADDRESS, dissolution.getCreatedBy().getIpAddress());
        assertNotNull(dissolution.getCreatedBy().getDateTime());
    }

    @Nested
    @DisplayName("Transaction Model Dissolution")
    class TransactionModelDissolution {
        private Transaction transaction;
        private DissolutionUserData userData;

        @BeforeEach
        void setup() {
            transaction = TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.OPEN).build();

            userData = new DissolutionUserData();
            userData.setEmail(EMAIL);
            userData.setIpAddress(IP_ADDRESS);
            userData.setUserId(USER_ID);
        }

        @ParameterizedTest
        @CsvSource({
                "ltd,    DS01",
                "llp,    LLDS01"
        })
        void mapToDraftDissolution_mapsApplicationType(String companyType, ApplicationType expectedType) {
            final CompanyProfile company = aCompany().withCompanyNumber(COMPANY_NUMBER)
                    .withCompanyName(COMPANY_NAME)
                    .withType(companyType)
                    .build();
            final Dissolution dissolution = requestMapper.mapToDraftDissolution(transaction, company, userData);

            assertFalse(dissolution.getActive());
            assertNotNull(dissolution.getModifiedDateTime());
            assertEquals(transaction.getId(), dissolution.getTransactionId());
            assertEquals(DissolutionStatus.DRAFT, dissolution.getStatus());

            DissolutionApplication application = dissolution.getData().getApplication();
            assertEquals(expectedType, application.getType());

            assertNull(application.getBarcode());
            assertNull(application.getReference());
            assertNull(application.getStatus());
            assertNull(dissolution.getData().getDirectors());

            assertEquals(COMPANY_NUMBER, dissolution.getCompany().getNumber());
            assertEquals(COMPANY_NAME, dissolution.getCompany().getName());

            assertEquals(USER_ID, dissolution.getCreatedBy().getUserId());
            assertEquals(EMAIL, dissolution.getCreatedBy().getEmail());
            assertEquals(IP_ADDRESS, dissolution.getCreatedBy().getIpAddress());
            assertNotNull(dissolution.getCreatedBy().getDateTime());
        }
    }
}
