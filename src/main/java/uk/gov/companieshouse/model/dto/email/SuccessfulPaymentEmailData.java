package uk.gov.companieshouse.model.dto.email;

public class SuccessfulPaymentEmailData {
    private String to;
    private String subject;
    private String dissolutionReferenceNumber;
    private String companyNumber;
    private String companyName;
    private String env;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

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

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
}
