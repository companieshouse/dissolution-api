package uk.gov.companieshouse.model.dto.chips.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class ChipsCorporateBody {

    private String incorporationNumber;
    private String corporateBodyName;

    @JacksonXmlElementWrapper(localName = "officers")
    @JacksonXmlProperty(localName = "officer")
    private List<ChipsOfficer> officers;

    public String getIncorporationNumber() {
        return incorporationNumber;
    }

    public void setIncorporationNumber(String incorporationNumber) {
        this.incorporationNumber = incorporationNumber;
    }

    public String getCorporateBodyName() {
        return corporateBodyName;
    }

    public void setCorporateBodyName(String corporateBodyName) {
        this.corporateBodyName = corporateBodyName;
    }

    public List<ChipsOfficer> getOfficers() {
        return officers;
    }

    public void setOfficers(List<ChipsOfficer> officers) {
        this.officers = officers;
    }
}
