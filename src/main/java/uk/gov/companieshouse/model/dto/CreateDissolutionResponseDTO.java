package uk.gov.companieshouse.model.dto;

public class CreateDissolutionResponseDTO {

    private String applicationReferenceNumber;
    private CreateDissolutionLinksDTO links;

    public String getApplicationReferenceNumber() {
        return applicationReferenceNumber;
    }

    public void setApplicationReferenceNumber(String applicationReferenceNumber) {
        this.applicationReferenceNumber = applicationReferenceNumber;
    }

    public CreateDissolutionLinksDTO getLinks() {
        return links;
    }

    public void setLinks(CreateDissolutionLinksDTO links) {
        this.links = links;
    }
}
