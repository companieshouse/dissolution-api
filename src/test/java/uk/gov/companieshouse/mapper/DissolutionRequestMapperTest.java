package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.db.DissolutionApplication;
import uk.gov.companieshouse.model.db.DissolutionDirector;
import uk.gov.companieshouse.model.dto.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.DirectorRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatusEnum;
import uk.gov.companieshouse.model.enums.ApplicationTypeEnum;

import java.util.Arrays;

import static org.junit.Assert.*;

public class DissolutionRequestMapperTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String USER_ID = "user123";
    private static final String EMAIL = "user@mail.com";
    private static final String IP_ADDRESS = "192.168.0.1";
    private static final String REFERENCE = "ABC123";

    private final DissolutionRequestMapper mapper = new DissolutionRequestMapper();

    @Test
    public void mapToDissolution_setsModifiedDateTime() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();

        final Dissolution dissolution = mapper.mapToDissolution(body, COMPANY_NUMBER, USER_ID, EMAIL, IP_ADDRESS, REFERENCE);

        assertNotNull(dissolution.getModifiedDateTime());
    }

    @Test
    public void mapToDissolution_setsApplicationData_includingDefaultStatus() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();

        final Dissolution dissolution = mapper.mapToDissolution(body, COMPANY_NUMBER, USER_ID, EMAIL, IP_ADDRESS, REFERENCE);

        assertEquals(REFERENCE, dissolution.getData().getApplication().getReference());
        assertEquals(ApplicationStatusEnum.PENDING_APPROVAL, dissolution.getData().getApplication().getStatus());
        assertEquals(ApplicationTypeEnum.DS01, dissolution.getData().getApplication().getType());
    }

    @Test
    public void mapToDissolution_setsDirectorsToSignFromRequestBody() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();

        final DirectorRequest director1 = DissolutionFixtures.generateDirectorRequest();
        director1.setName("Director who will sign themselves");
        director1.setEmail("director@mail.com");
        director1.setOnBehalfName(null);

        final DirectorRequest director2 = DissolutionFixtures.generateDirectorRequest();
        director2.setName("Director who will let someone sign on behalf of them");
        director2.setEmail("accountant@mail.com");
        director2.setOnBehalfName("Mr Accountant");

        body.setDirectors(Arrays.asList(director1, director2));

        final Dissolution dissolution = mapper.mapToDissolution(body, COMPANY_NUMBER, USER_ID, EMAIL, IP_ADDRESS, REFERENCE);

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

        final Dissolution dissolution = mapper.mapToDissolution(body, COMPANY_NUMBER, USER_ID, EMAIL, IP_ADDRESS, REFERENCE);

        assertEquals(COMPANY_NUMBER, dissolution.getCompany().getNumber());
        assertEquals("PLACEHOLDER COMPANY NAME", dissolution.getCompany().getName());
    }

    @Test
    public void mapToDissolution_setsCreatedByInformation() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();

        final Dissolution dissolution = mapper.mapToDissolution(body, COMPANY_NUMBER, USER_ID, EMAIL, IP_ADDRESS, REFERENCE);

        assertEquals(USER_ID, dissolution.getCreatedBy().getUserId());
        assertEquals(EMAIL, dissolution.getCreatedBy().getEmail());
        assertEquals(IP_ADDRESS, dissolution.getCreatedBy().getIpAddress());
        assertNotNull(dissolution.getCreatedBy().getDateTime());
    }
}
