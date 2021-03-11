package uk.gov.companieshouse.model.dto.email;

public class SupportNotificationEmailData extends EmailData {
    private String dissolutionReferenceNumber;
    private String companyNumber;
    private String companyName;
    private String timestamp;
    private String status;
    private int retryCounter;

    public String getDissolutionReferenceNumber() {
        return dissolutionReferenceNumber;
    }

    public void setDissolutionReferenceNumber(String dissolutionReferenceNumber) {
        this.dissolutionReferenceNumber = dissolutionReferenceNumber;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getRetryCounter() {
        return retryCounter;
    }

    public void setRetryCounter(int retryCounter) {
        this.retryCounter = retryCounter;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
