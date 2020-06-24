package uk.gov.companieshouse.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DissolutionCreateResponse {

    @JsonProperty("application_reference_number")
    private String applicationReferenceNumber;
    private DissolutionCreateLinks links;

    public String getApplicationReferenceNumber() {
        return applicationReferenceNumber;
    }

    public void setApplicationReferenceNumber(String applicationReferenceNumber) {
        this.applicationReferenceNumber = applicationReferenceNumber;
    }

    public DissolutionCreateLinks getLinks() {
        return links;
    }

    public void setLinks(DissolutionCreateLinks links) {
        this.links = links;
    }
}
