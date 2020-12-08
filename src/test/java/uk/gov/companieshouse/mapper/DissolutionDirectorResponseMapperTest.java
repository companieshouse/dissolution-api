package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionLinks;

import static org.junit.Assert.assertEquals;

class DissolutionDirectorResponseMapperTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REFERENCE = "reference123";

    private final DissolutionDirectorResponseMapper directorMapper = new DissolutionDirectorResponseMapper();

    @Test
    void mapToDissolutionDirectorPatchResponse_mapsCompanyNumberAndReferenceToDissolutionLinks() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        dissolution.getCompany().setNumber(COMPANY_NUMBER);
        dissolution.getData().getApplication().setReference(REFERENCE);

        final DissolutionDirectorPatchResponse result = directorMapper.mapToDissolutionDirectorPatchResponse(dissolution);

        final DissolutionLinks links = result.getLinks();

        assertEquals(String.format("/dissolution-request/%s", COMPANY_NUMBER), links.getSelf());
        assertEquals(String.format("/dissolution-request/%s/payment", REFERENCE), links.getPayment());
    }
}
