package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

public class DissolutionGetResponseTestDataBuilder {

    private ApplicationStatus applicationStatus = ApplicationStatus.PENDING_APPROVAL;
    private DissolutionStatus dissolutionStatus = null;
    private String applicationReference = "app-ref-123";
    private String paymentReference = null;
    private String transactionId = null;

    public static DissolutionGetResponseTestDataBuilder aDissolutionGetResponse() {
        return new DissolutionGetResponseTestDataBuilder();
    }

    public DissolutionGetResponseTestDataBuilder withApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
        return this;
    }

    public DissolutionGetResponseTestDataBuilder withDissolutionStatus(DissolutionStatus dissolutionStatus) {
        this.dissolutionStatus = dissolutionStatus;
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

    public DissolutionGetResponseTestDataBuilder withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public DissolutionGetResponse build() {
        final DissolutionGetResponse response = new DissolutionGetResponse();
        response.setApplicationStatus(applicationStatus);
        response.setDissolutionStatus(dissolutionStatus);
        response.setApplicationReference(applicationReference);
        response.setPaymentReference(paymentReference);
        response.setTransactionId(transactionId);
        return response;
    }
}
