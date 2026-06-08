package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;

public class DissolutionDirectorTestDataBuilder {

    private String officerId = "abc123";
    private String name = "DOE, John James";
    private String email = "john@doe.com";
    private String onBehalfName = null;

    public static DissolutionDirectorTestDataBuilder aDissolutionDirector() {
        return new DissolutionDirectorTestDataBuilder();
    }

    public DissolutionDirectorTestDataBuilder withOfficerId(String officerId) {
        this.officerId = officerId;
        return this;
    }

    public DissolutionDirectorTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public DissolutionDirectorTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public DissolutionDirectorTestDataBuilder withOnBehalfName(String onBehalfName) {
        this.onBehalfName = onBehalfName;
        return this;
    }

    public DissolutionDirector build() {
        final DissolutionDirector director = new DissolutionDirector();
        director.setOfficerId(officerId);
        director.setName(name);
        director.setEmail(email);
        director.setOnBehalfName(onBehalfName);
        return director;
    }
}

