package uk.gov.companieshouse.model.dto.dissolution;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DissolutionDirectorPatchRequest {

    @NotBlank
    @Email
    private String email;

    @JsonProperty("on_behalf_name")
    @Size(min = 1, max = 250)
    private String onBehalfName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOnBehalfName() {
        return onBehalfName;
    }

    public void setOnBehalfName(String onBehalfName) {
        this.onBehalfName = onBehalfName;
    }
}
