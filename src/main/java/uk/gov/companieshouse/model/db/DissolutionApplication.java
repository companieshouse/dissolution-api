package uk.gov.companieshouse.model.db;

import uk.gov.companieshouse.model.enums.DissolutionStatus;
import uk.gov.companieshouse.model.enums.DissolutionType;

public class DissolutionApplication {

    private String reference;
    private DissolutionType type;
    private DissolutionStatus status;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public DissolutionType getType() {
        return type;
    }

    public void setType(DissolutionType type) {
        this.type = type;
    }

    public DissolutionStatus getStatus() {
        return status;
    }

    public void setStatus(DissolutionStatus status) {
        this.status = status;
    }
}
