package uk.gov.companieshouse.model.dto.companyprofile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CompanyProfile(
        @JsonProperty("company_name") String companyName,
        @JsonProperty("type") String type,
        @JsonProperty("company_number") String companyNumber,
        @JsonProperty("company_status") String companyStatus) {
}
