package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

public class DirectorRequestTestDataBuilder {

    private String officerId = "abc123";
    private String email = "user@mail.com";
    private String onBehalfName;

    public static DirectorRequestTestDataBuilder aDirectorRequest() {
        return new DirectorRequestTestDataBuilder();
    }

    public DirectorRequestTestDataBuilder withOfficerId(String officerId) {
        this.officerId = officerId;
        return this;
    }

    public DirectorRequestTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public DirectorRequestTestDataBuilder withOnBehalfName(String onBehalfName) {
        this.onBehalfName = onBehalfName;
        return this;
    }

    public DirectorRequest build() {
        final DirectorRequest director = new DirectorRequest();
        director.setOfficerId(officerId);
        director.setEmail(email);
        director.setOnBehalfName(onBehalfName);
        return director;
    }
}
