package uk.gov.companieshouse.model.db.dissolution;

import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;

public class DissolutionApplication {

    private String barcode;

    private String reference;

    private ApplicationType type;

    private ApplicationStatus status;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public ApplicationType getType() {
        return type;
    }

    public void setType(ApplicationType type) {
        this.type = type;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
