package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionLinks;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.*;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionCertificate;
import static uk.gov.companieshouse.model.Constants.DISSOLUTION_KIND;

public class DissolutionResponseMapperTest {
    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NAME = "Example Name";
    private static final String ETAG = "ETag123";
    private static final ApplicationStatus STATUS = ApplicationStatus.PAID;
    private static final ApplicationType TYPE = ApplicationType.DS01;
    private static final String REFERENCE = "reference123";
    private static final LocalDateTime CREATED_AT = LocalDateTime.now();
    private static final String EMAIL = "user@email.com";

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

        dissolution.getCreatedBy().setEmail(EMAIL);
        dissolution.getCreatedBy().setDateTime(CREATED_AT);

        final DissolutionGetResponse result = mapper.mapToDissolutionGetResponse(dissolution);

        assertEquals(ETAG, result.getETag());
        assertEquals(DISSOLUTION_KIND, result.getKind());
        assertEquals("/dissolution-request/12345678", result.getLinks().getSelf());
        assertEquals("/dissolution-request/12345678/payment", result.getLinks().getPayment());
        assertEquals(STATUS, result.getApplicationStatus());
        assertEquals(REFERENCE, result.getApplicationReference());
        assertEquals(TYPE, result.getApplicationType());
        assertEquals(COMPANY_NAME, result.getCompanyName());
        assertEquals(COMPANY_NUMBER, result.getCompanyNumber());
        assertEquals(Timestamp.valueOf(CREATED_AT), result.getCreatedAt());
        assertEquals(EMAIL, result.getCreatedBy());
    }

    @Test
    public void mapToDissolutionGetResponse_mapsDirectorInDissolutionToDissolutionGetDirectors() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionDirector director1 = DissolutionFixtures.generateDissolutionDirector();
        director1.setOfficerId("abc123");
        director1.setName("Director who will sign themselves");
        director1.setEmail("director@mail.com");
        director1.setOnBehalfName(null);
        director1.setDirectorApproval(DissolutionFixtures.generateDirectorApproval());
        final Timestamp expectedTimestamp1 = Timestamp.valueOf(director1.getDirectorApproval().getDateTime());

        final DissolutionDirector director2 = DissolutionFixtures.generateDissolutionDirector();
        director2.setOfficerId("def456");
        director2.setName("Director who will let someone sign on behalf of them");
        director2.setEmail("accountant@mail.com");
        director2.setOnBehalfName("Mr Accountant");
        director2.setDirectorApproval(DissolutionFixtures.generateDirectorApproval());
        final Timestamp expectedTimestamp2 = Timestamp.valueOf(director2.getDirectorApproval().getDateTime());

        dissolution.getData().setDirectors(Arrays.asList(director1, director2));

        final DissolutionGetResponse result = mapper.mapToDissolutionGetResponse(dissolution);

        assertEquals("abc123", result.getDirectors().get(0).getOfficerId());
        assertEquals("Director who will sign themselves", result.getDirectors().get(0).getName());
        assertEquals("director@mail.com", result.getDirectors().get(0).getEmail());
        assertNull(result.getDirectors().get(0).getOnBehalfName());
        assertNotNull(result.getDirectors().get(0).getApprovedAt());
        assertEquals(expectedTimestamp1, result.getDirectors().get(0).getApprovedAt());

        assertEquals("def456", result.getDirectors().get(1).getOfficerId());
        assertEquals("Director who will let someone sign on behalf of them", result.getDirectors().get(1).getName());
        assertEquals("accountant@mail.com", result.getDirectors().get(1).getEmail());
        assertEquals("Mr Accountant", result.getDirectors().get(1).getOnBehalfName());
        assertNotNull(result.getDirectors().get(1).getApprovedAt());
        assertEquals(expectedTimestamp2, result.getDirectors().get(1).getApprovedAt());

        assertEquals(2, result.getDirectors().size());
    }

    @Test
    public void mapToDissolutionGetResponse_setsCertificateFields_ifCertificateIsAvailable() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        final DissolutionCertificate certificate = generateDissolutionCertificate();
        certificate.setBucket("some-bucket");
        certificate.setKey("some-key");

        dissolution.setCertificate(certificate);

        final DissolutionGetResponse result = mapper.mapToDissolutionGetResponse(dissolution);

        assertEquals("some-bucket", result.getCertificateBucket());
        assertEquals("some-key", result.getCertificateKey());
    }

    @Test
    public void mapToDissolutionGetResponse_doesNotSetCertificateFields_ifCertificateIsNotAvailable() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.setCertificate(null);

        final DissolutionGetResponse result = mapper.mapToDissolutionGetResponse(dissolution);

        assertNull(result.getCertificateBucket());
        assertNull(result.getCertificateKey());
    }

    @Test
    public void mapToDissolutionPatchResponse_mapsCompanyNumberToDissolutionLinks() {
        final DissolutionPatchResponse result = mapper.mapToDissolutionPatchResponse(COMPANY_NUMBER);
        final DissolutionLinks link = result.getLinks();
        final String expectedSelfLink = String.format("/dissolution-request/%s", COMPANY_NUMBER);
        final String expectedPayLink = String.format("/dissolution-request/%s/payment", COMPANY_NUMBER);

        assertEquals(expectedSelfLink, link.getSelf());
        assertEquals(expectedPayLink, link.getPayment());
    }
}
