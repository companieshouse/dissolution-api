package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.enums.ApplicationStatus;

public class DissolutionGetResponseTestDataBuilder {

    private ApplicationStatus applicationStatus = ApplicationStatus.PENDING_APPROVAL;
    private String applicationReference = "app-ref-123";
    private String paymentReference = null;

    public static DissolutionGetResponseTestDataBuilder aDissolutionGetResponse() {
        return new DissolutionGetResponseTestDataBuilder();
    }

    public DissolutionGetResponseTestDataBuilder withApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
        return this;
    }

    public DissolutionGetResponseTestDataBuilder withApplicationReference(String applicationReference) {
        this.applicationReference = applicationReference;
        return this;
    }

    public DissolutionGetResponseTestDataBuilder withPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
        return this;
    }

    public DissolutionGetResponse build() {
        final DissolutionGetResponse response = new DissolutionGetResponse();
        response.setApplicationStatus(applicationStatus);
        response.setApplicationReference(applicationReference);
        response.setPaymentReference(paymentReference);
        return response;
    }
}
