package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;

public class DissolutionDirectorPatchRequestTestDataBuilder {

    private String email = "abc123@mail.com";
    private String onBehalfName = "asd";

    public static DissolutionDirectorPatchRequestTestDataBuilder aDissolutionDirectorPatchRequest() {
        return new DissolutionDirectorPatchRequestTestDataBuilder();
    }

    public DissolutionDirectorPatchRequestTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public DissolutionDirectorPatchRequestTestDataBuilder withOnBehalfName(String onBehalfName) {
        this.onBehalfName = onBehalfName;
        return this;
    }

    public DissolutionDirectorPatchRequest build() {
        final DissolutionDirectorPatchRequest request = new DissolutionDirectorPatchRequest();
        request.setEmail(email);
        request.setOnBehalfName(onBehalfName);
        return request;
    }
}

