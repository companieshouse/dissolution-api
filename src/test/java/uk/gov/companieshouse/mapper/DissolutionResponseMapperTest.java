package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.db.DissolutionDirector;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.DissolutionGetResponse;
import uk.gov.companieshouse.model.enums.DissolutionStatus;
import uk.gov.companieshouse.model.enums.DissolutionType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.companieshouse.model.Constants.DISSOLUTION_KIND;

public class DissolutionResponseMapperTest {
    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NAME = "Example Name";
    private static final String ETAG = "ETag123";
    private static final DissolutionStatus STATUS = DissolutionStatus.PAID;
    private static final DissolutionType TYPE = DissolutionType.DS01;
    private static final String REFERENCE = "reference123";
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final String USER_ID = "user123";

    private final DissolutionResponseMapper mapper = new DissolutionResponseMapper();

    @Test
    public void mapToDissolutionCreateResponse_mapsDissolutionReference() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getData().getApplication().setReference("ABC123");

        final DissolutionCreateResponse result = mapper.mapToDissolutionCreateResponse(dissolution);

        assertEquals("ABC123", result.getApplicationReferenceNumber());
    }

    @Test
    public void mapToDissolutionCreateResponse_generatesLinks() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getCompany().setNumber("12345678");

        final DissolutionCreateResponse result = mapper.mapToDissolutionCreateResponse(dissolution);

        assertEquals("/dissolution-request/12345678", result.getLinks().getSelf());
        assertEquals("/dissolution-request/12345678/payment", result.getLinks().getPayment());
    }

    @Test
    public void mapToDissolutionGetResponse_mapsToGetResponse() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        dissolution.getCompany().setName(COMPANY_NAME);
        dissolution.getCompany().setNumber(COMPANY_NUMBER);

        dissolution.getData().setETag(ETAG);
        dissolution.getData().getApplication().setReference(REFERENCE);
        dissolution.getData().getApplication().setStatus(STATUS);
        dissolution.getData().getApplication().setType(TYPE);

        dissolution.getCreatedBy().setUserId(USER_ID);
        dissolution.getCreatedBy().setDateTime(CREATED_AT);

        final DissolutionGetResponse result = mapper.mapToDissolutionGetResponse(dissolution);

        assertEquals(ETAG, result.getETag());
        assertEquals(DISSOLUTION_KIND, result.getKind());
        assertEquals("/dissolution-request/12345678", result.getLinks().getSelf());
        assertEquals("/dissolution-request/12345678/payment", result.getLinks().getPayment());
        assertEquals(STATUS, result.getDissolutionStatus());
        assertEquals(REFERENCE, result.getApplicationReference());
        assertEquals(TYPE, result.getDissolutionType());
        assertEquals(COMPANY_NAME, result.getCompanyName());
        assertEquals(COMPANY_NUMBER, result.getCompanyNumber());
        assertEquals(Timestamp.valueOf(CREATED_AT), result.getCreatedAt());
        assertEquals(USER_ID, result.getCreatedBy());
    }

    @Test
    public void mapToDissolutionGetResponse_mapsDirectorInDissolutionToDissolutionGetDirectors() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director1 = DissolutionFixtures.generateDissolutionDirector();
        director1.setName("Director who will sign themselves");
        director1.setEmail("director@mail.com");
        director1.setOnBehalfName(null);

        final DissolutionDirector director2 = DissolutionFixtures.generateDissolutionDirector();
        director2.setName("Director who will let someone sign on behalf of them");
        director2.setEmail("accountant@mail.com");
        director2.setOnBehalfName("Mr Accountant");

        dissolution.getData().setDirectors(Arrays.asList(director1, director2));

        final DissolutionGetResponse result = mapper.mapToDissolutionGetResponse(dissolution);

        assertEquals("Director who will sign themselves", result.getDirectors().get(0).getName());
        assertEquals("director@mail.com", result.getDirectors().get(0).getEmail());
        assertNull(result.getDirectors().get(0).getOnBehalfName());
        assertNull(result.getDirectors().get(0).getApprovedAt()); // TODO change once approvedAt is implemented

        assertEquals("Director who will let someone sign on behalf of them", result.getDirectors().get(1).getName());
        assertEquals("accountant@mail.com", result.getDirectors().get(1).getEmail());
        assertEquals("Mr Accountant", result.getDirectors().get(1).getOnBehalfName());
        assertNull(result.getDirectors().get(1).getApprovedAt()); // TODO change once approvedAt is implemented

        assertEquals(2, result.getDirectors().size());
    }
}
