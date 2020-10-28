package uk.gov.companieshouse.model.dto.dissolution;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;

public class DissolutionPatchRequest {

    @NotBlank
    @JsonProperty("officer_id")
    private String officerId;

    @AssertTrue
    @JsonProperty("has_approved")
    private boolean hasApproved;

    @NotBlank
    @JsonProperty("ip_address")
    private String ipAddress;

    public String getOfficerId() {
        return officerId;
    }

    public void setOfficerId(String officerId) {
        this.officerId = officerId;
    }

    public boolean getHasApproved() {
        return hasApproved;
    }

    public void setHasApproved(boolean hasApproved) {
        this.hasApproved = hasApproved;
    }

    public String getIpAddress() { return ipAddress; }

    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
