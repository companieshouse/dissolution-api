package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.CompanyType;

import java.util.Arrays;

import static org.junit.Assert.*;

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
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);

        final Dissolution dissolution = mapper.mapToDissolution(body, company, USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertNotNull(dissolution.getModifiedDateTime());
    }

    @Test
    public void mapToDissolution_setsApplicationData_includingDefaultStatusForDS01() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);
        company.setType(CompanyType.PLC.getValue());


        final Dissolution dissolution = mapper.mapToDissolution(body, company, USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(BARCODE, dissolution.getData().getApplication().getBarcode());
        assertEquals(REFERENCE, dissolution.getData().getApplication().getReference());
        assertEquals(ApplicationStatus.PENDING_APPROVAL, dissolution.getData().getApplication().getStatus());
        assertEquals(ApplicationType.DS01, dissolution.getData().getApplication().getType());
    }

    @Test
    public void mapToDissolution_setsApplicationData_includingDefaultStatusForLLDS01() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);
        company.setType(CompanyType.LLP.getValue());

        final Dissolution dissolution = mapper.mapToDissolution(body, company, USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(BARCODE, dissolution.getData().getApplication().getBarcode());
        assertEquals(REFERENCE, dissolution.getData().getApplication().getReference());
        assertEquals(ApplicationStatus.PENDING_APPROVAL, dissolution.getData().getApplication().getStatus());
        assertEquals(ApplicationType.LLDS01, dissolution.getData().getApplication().getType());
    }

    @Test
    public void mapToDissolution_setsDirectorsToSignFromRequestBody() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);

        final DirectorRequest director1 = DissolutionFixtures.generateDirectorRequest();
        director1.setName("Director who will sign themselves");
        director1.setEmail("director@mail.com");
        director1.setOnBehalfName(null);

        final DirectorRequest director2 = DissolutionFixtures.generateDirectorRequest();
        director2.setName("Director who will let someone sign on behalf of them");
        director2.setEmail("accountant@mail.com");
        director2.setOnBehalfName("Mr Accountant");

        body.setDirectors(Arrays.asList(director1, director2));

        final Dissolution dissolution = mapper.mapToDissolution(body, company, USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(2, dissolution.getData().getDirectors().size());

        final DissolutionDirector dissolutionDirector1 = dissolution.getData().getDirectors().get(0);
        assertEquals("Director who will sign themselves", dissolutionDirector1.getName());
        assertEquals("director@mail.com", dissolutionDirector1.getEmail());
        assertNull(dissolutionDirector1.getOnBehalfName());

        final DissolutionDirector dissolutionDirector2 = dissolution.getData().getDirectors().get(1);
        assertEquals("Director who will let someone sign on behalf of them", dissolutionDirector2.getName());
        assertEquals("accountant@mail.com", dissolutionDirector2.getEmail());
        assertEquals("Mr Accountant", dissolutionDirector2.getOnBehalfName());
    }

    @Test
    public void mapToDissolution_setsCompanyInformation() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);

        final Dissolution dissolution = mapper.mapToDissolution(body, company, USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(COMPANY_NUMBER, dissolution.getCompany().getNumber());
        assertEquals(COMPANY_NAME, dissolution.getCompany().getName());
    }

    @Test
    public void mapToDissolution_setsCreatedByInformation() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);

        final Dissolution dissolution = mapper.mapToDissolution(body, company, USER_ID, EMAIL, IP_ADDRESS, REFERENCE, BARCODE);

        assertEquals(USER_ID, dissolution.getCreatedBy().getUserId());
        assertEquals(EMAIL, dissolution.getCreatedBy().getEmail());
        assertEquals(IP_ADDRESS, dissolution.getCreatedBy().getIpAddress());
        assertNotNull(dissolution.getCreatedBy().getDateTime());
    }
}
