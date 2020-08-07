package uk.gov.companieshouse.model.dto.chips.xml;

import java.util.List;

public class ChipsCorporateBody {

    private String incorporationNumber;
    private String corporateBodyName;

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
