package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;

public class DissolutionDirectorFixtures {
    public static DissolutionDirectorPatchRequest generateDissolutionPatchDirectorRequest() {
        final DissolutionDirectorPatchRequest request = new DissolutionDirectorPatchRequest();

        request.setEmail("abc123@mail.com");
        request.setOnBehalfName("asd");

        return request;
    }
}
