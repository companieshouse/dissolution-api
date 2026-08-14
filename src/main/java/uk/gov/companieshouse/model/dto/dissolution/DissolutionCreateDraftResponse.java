package uk.gov.companieshouse.model.dto.dissolution;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DissolutionCreateDraftResponse {

    @JsonProperty("dissolution_id")
    private String dissolution_id;
    private DissolutionLinks links;

    public String getDissolutionId() {
        return dissolution_id;
    }

    public void setDissolutionId(String dissolutionId) {
        this.dissolution_id = dissolutionId;
    }

    public DissolutionLinks getLinks() {
        return links;
    }

    public void setLinks(DissolutionLinks links) {
        this.links = links;
    }
}