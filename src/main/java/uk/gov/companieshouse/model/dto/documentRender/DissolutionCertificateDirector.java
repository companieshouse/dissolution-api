package uk.gov.companieshouse.model.dto.documentRender;

import java.sql.Timestamp;

public class DissolutionCertificateDirector {

    private String name;
    private String onBehalfName;
    private Timestamp approvalDate;

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

    public Timestamp getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Timestamp approvalDate) {
        this.approvalDate = approvalDate;
    }
}
