package uk.gov.companieshouse.model.dto.companyprofile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyProfile {
    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("type")
    private String type;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("company_status")
    private String companyStatus;

    private CompanyProfile(Builder builder){
        this.companyName = builder.companyName;
        this.type = builder.type;
        this.companyNumber = builder.companyNumber;
        this.companyStatus = builder.companyStatus;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyStatus() {
        return companyStatus;
    }

    public void setCompanyStatus(String companyStatus) {
        this.companyStatus = companyStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class Builder{
        private String companyName;
        private String type;
        private String companyNumber;
        private String companyStatus;

        public Builder withCompanyName(String companyName){
            this.companyName = companyName;

            return this;
        }

        public Builder withType(String type){
            this.type = type;

            return this;
        }

        public Builder withCompanyNumber(String companyNumber){
            this.companyNumber = companyNumber;

            return this;
        }

        public Builder withCompanyStatus(String companyStatus){
            this.companyStatus = companyStatus;

            return this;
        }

        public CompanyProfile build(){
            return new CompanyProfile(this);
        }
    }
}
