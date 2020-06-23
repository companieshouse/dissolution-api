package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.dto.CreateDissolutionResponseDTO;

import static org.junit.Assert.assertEquals;

public class DissolutionResponseMapperTest {

    private final DissolutionResponseMapper mapper = new DissolutionResponseMapper();

    @Test
    public void mapToCreateDissolutionResponse_mapsDissolutionReference() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getData().getApplication().setReference("ABC123");

        final CreateDissolutionResponseDTO result = mapper.mapToCreateDissolutionResponse(dissolution);

        assertEquals("ABC123", result.getApplicationReferenceNumber());
    }

    @Test
    public void mapToCreateDissolutionResponse_generatesLinks() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        dissolution.getCompany().setNumber("12345678");

        final CreateDissolutionResponseDTO result = mapper.mapToCreateDissolutionResponse(dissolution);

        assertEquals("/dissolution-request/12345678", result.getLinks().getSelf());
        assertEquals("/dissolution-request/12345678/payment", result.getLinks().getPayment());
    }
}
