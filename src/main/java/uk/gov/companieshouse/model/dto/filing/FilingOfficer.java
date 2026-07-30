package uk.gov.companieshouse.model.dto.filing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilingOfficer {

    @JsonProperty("person_name")
    private FilingPersonName personName;

    @JsonProperty("sign_date")
    private String signDate;

    @JsonProperty("email")
    private String email;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("on_behalf_name")
    private String onBehalfName;

    public FilingPersonName getPersonName() {
        return personName;
    }

    public void setPersonName(FilingPersonName personName) {
        this.personName = personName;
    }

    public String getSignDate() {
        return signDate;
    }

    public void setSignDate(String signDate) {
        this.signDate = signDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getOnBehalfName() {
        return onBehalfName;
    }

    public void setOnBehalfName(String onBehalfName) {
        this.onBehalfName = onBehalfName;
    }
}
