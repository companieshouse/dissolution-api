package uk.gov.companieshouse.model.dto.dissolution;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DissolutionPatchRequest {
    private String email;

    @JsonProperty("has_approved")
    private boolean hasApproved;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getHasApproved() {
        return hasApproved;
    }

    public void setHasApproved(boolean hasApproved) {
        this.hasApproved = hasApproved;
    }
}
