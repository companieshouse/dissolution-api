package uk.gov.companieshouse.model.dto.chips.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "form")
public class ChipsFormData {

    @JacksonXmlProperty(isAttribute = true)
    private ChipsFormType type;

    @JacksonXmlProperty(isAttribute = true)
    private int version;

    private ChipsFilingDetails filingDetails;

    private ChipsCorporateBody corporateBody;

    public ChipsFormType getType() {
        return type;
    }

    public void setType(ChipsFormType type) {
        this.type = type;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ChipsFilingDetails getFilingDetails() {
        return filingDetails;
    }

    public void setFilingDetails(ChipsFilingDetails filingDetails) {
        this.filingDetails = filingDetails;
    }

    public ChipsCorporateBody getCorporateBody() {
        return corporateBody;
    }

    public void setCorporateBody(ChipsCorporateBody corporateBody) {
        this.corporateBody = corporateBody;
    }
}
