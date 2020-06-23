package uk.gov.companieshouse.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.*;

public class DirectorRequestDTO {

    @NotBlank
    @Size(min = 1, max = 250)
    private String name;

    @NotBlank
    @Email
    private String email;

    @JsonProperty("on_behalf_name")
    @Size(min = 1, max = 250)
    private String onBehalfName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
