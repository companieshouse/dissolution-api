package uk.gov.companieshouse.model.dto.companyofficers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyOfficer {

    private String name;

    @JsonProperty("officer_role")
    private String officerRole;

    @JsonProperty("resigned_on")
    private String resignedOn;

    private CompanyOfficerLinks links;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOfficerRole() {
        return officerRole;
    }

    public void setOfficerRole(String officerRole) {
        this.officerRole = officerRole;
    }

    public String getResignedOn() {
        return resignedOn;
    }

    public void setResignedOn(String resignedOn) {
        this.resignedOn = resignedOn;
    }

    public CompanyOfficerLinks getLinks() {
        return links;
    }

    public void setLinks(CompanyOfficerLinks links) {
        this.links = links;
    }
}
