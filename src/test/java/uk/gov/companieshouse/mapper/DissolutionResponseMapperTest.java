package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;

import static org.junit.Assert.assertEquals;

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
}
