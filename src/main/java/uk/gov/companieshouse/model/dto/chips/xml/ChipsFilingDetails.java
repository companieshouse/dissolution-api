package uk.gov.companieshouse.model.dto.chips.xml;

public class ChipsFilingDetails {

    private String presenterDocumentReference;
    private ChipsPresenterDetails presenterDetails;
    private String receiptDate;
    private String submissionReference;
    private ChipsPayment payment;
    private String signDate;
    private String barcode;
    private String packageIdentifier;
    private int packageCount;
    private String method;

    public String getPresenterDocumentReference() {
        return presenterDocumentReference;
    }

    public void setPresenterDocumentReference(String presenterDocumentReference) {
        this.presenterDocumentReference = presenterDocumentReference;
    }

    public ChipsPresenterDetails getPresenterDetails() {
        return presenterDetails;
    }

    public void setPresenterDetails(ChipsPresenterDetails presenterDetails) {
        this.presenterDetails = presenterDetails;
    }

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getSubmissionReference() {
        return submissionReference;
    }

    public void setSubmissionReference(String submissionReference) {
        this.submissionReference = submissionReference;
    }

    public ChipsPayment getPayment() {
        return payment;
    }

    public void setPayment(ChipsPayment payment) {
        this.payment = payment;
    }

    public String getSignDate() {
        return signDate;
    }

    public void setSignDate(String signDate) {
        this.signDate = signDate;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getPackageIdentifier() {
        return packageIdentifier;
    }

    public void setPackageIdentifier(String packageIdentifier) {
        this.packageIdentifier = packageIdentifier;
    }

    public int getPackageCount() {
        return packageCount;
    }

    public void setPackageCount(int packageCount) {
        this.packageCount = packageCount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
