package uk.gov.companieshouse.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DissolutionCreateResponse {

    @JsonProperty("application_reference_number")
    private String applicationReferenceNumber;
    private DissolutionLinks links;

    public String getApplicationReferenceNumber() {
        return applicationReferenceNumber;
    }

    public void setApplicationReferenceNumber(String applicationReferenceNumber) {
        this.applicationReferenceNumber = applicationReferenceNumber;
    }

    public DissolutionLinks getLinks() {
        return links;
    }

    public void setLinks(DissolutionLinks links) {
        this.links = links;
    }
}
