package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionInitiationRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.gov.companieshouse.fixtures.DirectorRequestTestDataBuilder.aDirectorRequest;

public class DissolutionInitiationRequestTestDataBuilder {

    private List<DirectorRequest> directors = Collections.singletonList(aDirectorRequest().build());

    public static DissolutionInitiationRequestTestDataBuilder aDissolutionInitiationRequest() {
        return new DissolutionInitiationRequestTestDataBuilder();
    }

    public DissolutionInitiationRequestTestDataBuilder withDirectors(List<DirectorRequest> directors) {
        this.directors = directors;
        return this;
    }

    public DissolutionInitiationRequestTestDataBuilder withDirectorRequest(DirectorRequestTestDataBuilder director) {
        this.directors = Collections.singletonList(director.build());
        return this;
    }

    public DissolutionInitiationRequestTestDataBuilder withDirectorRequests(DirectorRequestTestDataBuilder... directorBuilders) {
        return withDirectors(Arrays.stream(directorBuilders).map(DirectorRequestTestDataBuilder::build).toList());
    }

    public DissolutionInitiationRequest build() {
        final DissolutionInitiationRequest request = new DissolutionInitiationRequest();
        request.setDirectors(directors);
        return request;
    }
}
