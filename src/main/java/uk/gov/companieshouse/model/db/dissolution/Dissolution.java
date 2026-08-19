package uk.gov.companieshouse.model.db.dissolution;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "dissolutions")
public class Dissolution {

    @Id
    private String id;

    @Field("modified_date_time")
    private LocalDateTime modifiedDateTime;

    private DissolutionData data;

    private Company company;

    @Field("created_by")
    private CreatedBy createdBy;

    private PaymentInformation payment;

    private DissolutionCertificate certificate;

    private DissolutionSubmission submission;

    private DissolutionVerdict verdict;

    boolean active;

    private DissolutionStatus status;

    @Field("status_history")
    private List<DissolutionStatusChanged> statusHistory = new ArrayList<>();

    @Field("submitted_at")
    private LocalDateTime submittedAt;

    @Field("transaction_id")
    private String transactionId;

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getModifiedDateTime() {
        return modifiedDateTime;
    }

    public void setModifiedDateTime(LocalDateTime modifiedDateTime) {
        this.modifiedDateTime = modifiedDateTime;
    }

    public DissolutionData getData() {
        return data;
    }

    public void setData(DissolutionData data) {
        this.data = data;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
    }

    public PaymentInformation getPaymentInformation() {
        return payment;
    }

    public void setPaymentInformation(PaymentInformation paymentInformation) {
        this.payment = paymentInformation;
    }

    public DissolutionCertificate getCertificate() {
        return certificate;
    }

    public void setCertificate(DissolutionCertificate certificate) {
        this.certificate = certificate;
    }

    public DissolutionSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(DissolutionSubmission submission) {
        this.submission = submission;
    }

    public DissolutionVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(DissolutionVerdict verdict) {
        this.verdict = verdict;
    }

    public DissolutionStatus getStatus() {
        return status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void changeStatus(DissolutionStatus newStatus, LocalDateTime changedAt) {
        statusHistory.add(new DissolutionStatusChanged(newStatus, changedAt));
        this.status = newStatus;
        if (newStatus == DissolutionStatus.SUBMITTED) {
            this.submittedAt = changedAt;
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
