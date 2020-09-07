package uk.gov.companieshouse.model.dto.chips.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "officer")
public class ChipsOfficer {

    private ChipsPersonName personName;
    private String signDate;
    private String onBehalfName;
    private String email;
    private String ipAddress;

    public ChipsPersonName getPersonName() {
        return personName;
    }

    public void setPersonName(ChipsPersonName personName) {
        this.personName = personName;
    }

    public String getSignDate() {
        return signDate;
    }

    public void setSignDate(String signDate) {
        this.signDate = signDate;
    }

    public String getOnBehalfName() {
        return onBehalfName;
    }

    public void setOnBehalfName(String onBehalfName) {
        this.onBehalfName = onBehalfName;
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
}
