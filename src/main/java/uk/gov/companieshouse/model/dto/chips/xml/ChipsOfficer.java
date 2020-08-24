package uk.gov.companieshouse.model.dto.chips.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "officer")
public class ChipsOfficer {

    private ChipsPersonName personName;
    private String signDate;

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
}
