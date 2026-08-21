package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.api.model.transaction.Filing;
import uk.gov.companieshouse.service.FilingStatus;

public class FilingTestDataBuilder {

    private String status = FilingStatus.PROCESSING.getValue();
    private String type = "dissolution#ds01";

    public static FilingTestDataBuilder aFiling() {
        return new FilingTestDataBuilder();
    }

    public FilingTestDataBuilder withStatus(FilingStatus status) {
        this.status = status.getValue();
        return this;
    }

    public FilingTestDataBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public Filing build() {
        final Filing filing = new Filing();
        filing.setStatus(status);
        filing.setType(type);
        return filing;
    }
}
