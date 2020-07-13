package uk.gov.companieshouse.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentLinks {

    private String self;

    @JsonProperty("dissolution_request")
    private String dissolutionRequest;

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public String getDissolutionRequest() {
        return dissolutionRequest;
    }

    public void setDissolutionRequest(String dissolutionRequest) {
        this.dissolutionRequest = dissolutionRequest;
    }
}
