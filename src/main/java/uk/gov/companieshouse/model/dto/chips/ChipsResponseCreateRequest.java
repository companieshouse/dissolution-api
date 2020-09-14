package uk.gov.companieshouse.model.dto.chips;

import uk.gov.companieshouse.model.enums.VerdictResult;

public class ChipsResponseCreateRequest {

    private String barcode;

    private String submissionReference;

    private VerdictResult status;

    private RejectReason[] rejectReasons;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSubmissionReference() {
        return submissionReference;
    }

    public void setSubmissionReference(String submissionReference) {
        this.submissionReference = submissionReference;
    }

    public VerdictResult getStatus() {
        return status;
    }

    public void setStatus(VerdictResult status) {
        this.status = status;
    }

    public RejectReason[] getRejectReasons() {
        return rejectReasons;
    }

    public void setRejectReasons(RejectReason[] rejectReasons) {
        this.rejectReasons = rejectReasons;
    }
}
