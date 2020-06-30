package uk.gov.companieshouse.model.db;

import uk.gov.companieshouse.model.enums.ApplicationStatusEnum;
import uk.gov.companieshouse.model.enums.ApplicationTypeEnum;

public class DissolutionApplication {

    private String reference;
    private ApplicationTypeEnum type;
    private ApplicationStatusEnum status;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ApplicationTypeEnum getType() {
        return type;
    }

    public void setType(ApplicationTypeEnum type) {
        this.type = type;
    }

    public ApplicationStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatusEnum status) {
        this.status = status;
    }
}
