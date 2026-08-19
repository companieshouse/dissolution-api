package uk.gov.companieshouse.model.dto.dissolution;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DissolutionCreateDraftResponse {

    @JsonProperty("dissolution_id")
    private String dissolutionId;
    private DissolutionLinks links;

    public String getDissolutionId() {
        return dissolutionId;
    }

    public void setDissolutionId(String dissolutionId) {
        this.dissolutionId = dissolutionId;
    }

    public DissolutionLinks getLinks() {
        return links;
    }

    public void setLinks(DissolutionLinks links) {
        this.links = links;
    }
}
