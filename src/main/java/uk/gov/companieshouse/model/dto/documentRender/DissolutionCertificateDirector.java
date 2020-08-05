package uk.gov.companieshouse.model.dto.documentRender;

public class DissolutionCertificateDirector {

    private String name;
    private String onBehalfName;
    private String approvalDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOnBehalfName() {
        return onBehalfName;
    }

    public void setOnBehalfName(String onBehalfName) {
        this.onBehalfName = onBehalfName;
    }

    public String getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(String approvalDate) {
        this.approvalDate = approvalDate;
    }
}
