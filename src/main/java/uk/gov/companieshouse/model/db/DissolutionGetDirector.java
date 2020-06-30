package uk.gov.companieshouse.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;

public class DissolutionGetDirector {

    private String name;
    private String email;
    @JsonProperty("approved_at")
    private Timestamp approvedAt;
    @JsonProperty("on_behalf_name")
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

    public Timestamp getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Timestamp approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getOnBehalfName() {
        return onBehalfName;
    }

    public void setOnBehalfName(String onBehalfName) {
        this.onBehalfName = onBehalfName;
    }
}
