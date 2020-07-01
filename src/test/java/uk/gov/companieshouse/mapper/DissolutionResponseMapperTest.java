package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.db.DissolutionDirector;
import uk.gov.companieshouse.model.db.DissolutionGetDirector;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.DissolutionGetResponse;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionDirector;
import static uk.gov.companieshouse.model.Constants.DISSOLUTION_KIND;

public class DissolutionResponseMapperTest {

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
        final DissolutionGetResponse result = mapper.mapToDissolutionGetResponse(dissolution);

        assertEquals(dissolution.getData().getETag(), result.getETag());
        assertEquals(DISSOLUTION_KIND, result.getKind());
        assertEquals("/dissolution-request/12345678", result.getLinks().getSelf());
        assertEquals("/dissolution-request/12345678/payment", result.getLinks().getPayment());
        assertEquals(dissolution.getData().getApplication().getStatus(), result.getApplicationStatus());
        assertEquals(dissolution.getData().getApplication().getReference(), result.getApplicationReference());
        assertEquals(dissolution.getData().getApplication().getType(), result.getApplicationType());
        assertEquals(dissolution.getCompany().getName(), result.getCompanyName());
        assertEquals(dissolution.getCompany().getNumber(), result.getCompanyNumber());
        assertEquals(Timestamp.valueOf(dissolution.getCreatedBy().getDateTime()), result.getCreatedAt());
        assertEquals(dissolution.getCreatedBy().getUserId(), result.getCreatedBy());
        assertEquals(dissolution.getData().getDirectors().get(0).getName(), result.getDirectors().get(0).getName());
        assertEquals(dissolution.getData().getDirectors().get(0).getEmail(), result.getDirectors().get(0).getEmail());
        // TODO add test for createdAt field
    }

    @Test
    public void mapToDissolutionGetDirectors_mapsToGetDirectors() {
        List<DissolutionDirector> directors = Collections.singletonList(generateDissolutionDirector());

        final List<DissolutionGetDirector> result = mapper.mapToDissolutionGetDirectors(directors);

        assertEquals(directors.get(0).getName(), result.get(0).getName());
        assertEquals(directors.get(0).getEmail(), result.get(0).getEmail());
        // TODO add test for createdAt field
    }
}
