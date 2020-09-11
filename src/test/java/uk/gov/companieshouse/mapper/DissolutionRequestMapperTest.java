package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.CompanyType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;

public class DissolutionRequestMapperTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NAME = "ComComp";
    private static final String USER_ID = "user123";
    private static final String EMAIL = "user@mail.com";
    private static final String IP_ADDRESS = "192.168.0.1";
    private static final String REFERENCE = "ABC123";
    private static final String BARCODE = "B4RC0D3";

    private final DissolutionRequestMapper mapper = new DissolutionRequestMapper();

    @Test
    public void mapToDissolution_setsModifiedDateTime() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);

        final Dissolution dissolution = mapper.mapToDissolution(body, company, new HashMap<>(), USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertNotNull(dissolution.getModifiedDateTime());
    }

    @Test
    public void mapToDissolution_setsApplicationData_includingDefaultStatusForDS01() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);
        company.setType(CompanyType.PLC.getValue());

        final Dissolution dissolution = mapper.mapToDissolution(body, company, new HashMap<>(), USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(BARCODE, dissolution.getData().getApplication().getBarcode());
        assertEquals(REFERENCE, dissolution.getData().getApplication().getReference());
        assertTrue(dissolution.getActive());
        assertEquals(ApplicationStatus.PENDING_APPROVAL, dissolution.getData().getApplication().getStatus());
        assertEquals(ApplicationType.DS01, dissolution.getData().getApplication().getType());
    }

    @Test
    public void mapToDissolution_setsApplicationData_includingDefaultStatusForLLDS01() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);
        company.setType(CompanyType.LLP.getValue());

        final Dissolution dissolution = mapper.mapToDissolution(body, company, new HashMap<>(), USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(BARCODE, dissolution.getData().getApplication().getBarcode());
        assertEquals(REFERENCE, dissolution.getData().getApplication().getReference());
        assertTrue(dissolution.getActive());
        assertEquals(ApplicationStatus.PENDING_APPROVAL, dissolution.getData().getApplication().getStatus());
        assertEquals(ApplicationType.LLDS01, dissolution.getData().getApplication().getType());
    }

    @Test
    public void mapToDissolution_setsDirectorsToSignFromRequestBody() {
        final String officerId1 = "abc123";
        final String officerId2 = "def456";

        final CompanyOfficer companyDirector1 = generateCompanyOfficer();
        companyDirector1.setName("Director who will sign themselves");

        final CompanyOfficer companyDirector2 = generateCompanyOfficer();
        companyDirector2.setName("Director who will let someone sign on behalf of them");

        final Map<String, CompanyOfficer> companyDirectors = Map.of(
                officerId1, companyDirector1,
                officerId2, companyDirector2
        );

        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);

        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();

        final DirectorRequest selectedDirector1 = DissolutionFixtures.generateDirectorRequest();
        selectedDirector1.setOfficerId(officerId1);
        selectedDirector1.setEmail("director@mail.com");
        selectedDirector1.setOnBehalfName(null);

        final DirectorRequest selectedDirector2 = DissolutionFixtures.generateDirectorRequest();
        selectedDirector2.setOfficerId(officerId2);
        selectedDirector2.setEmail("accountant@mail.com");
        selectedDirector2.setOnBehalfName("Mr Accountant");

        body.setDirectors(Arrays.asList(selectedDirector1, selectedDirector2));

        final Dissolution dissolution = mapper.mapToDissolution(body, company, companyDirectors, USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(2, dissolution.getData().getDirectors().size());

        final DissolutionDirector dissolutionDirector1 = dissolution.getData().getDirectors().get(0);
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
    public void mapToDissolution_setsCompanyInformation() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);

        final Dissolution dissolution = mapper.mapToDissolution(body, company, new HashMap<>(), USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(COMPANY_NUMBER, dissolution.getCompany().getNumber());
        assertEquals(COMPANY_NAME, dissolution.getCompany().getName());
    }

    @Test
    public void mapToDissolution_setsCreatedByInformation() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        body.setDirectors(Collections.emptyList());
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);

        final Dissolution dissolution = mapper.mapToDissolution(body, company, new HashMap<>(), USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(USER_ID, dissolution.getCreatedBy().getUserId());
        assertEquals(EMAIL, dissolution.getCreatedBy().getEmail());
        assertEquals(IP_ADDRESS, dissolution.getCreatedBy().getIpAddress());
        assertNotNull(dissolution.getCreatedBy().getDateTime());
    }
}
