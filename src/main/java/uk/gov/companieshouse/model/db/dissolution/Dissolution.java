package uk.gov.companieshouse.model.db.dissolution;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.exception.DissolutionSignatoryNotFoundException;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

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

    public void assignSignatories(List<DissolutionDirector> directors) {
        if (this.data == null) {
            throw new IllegalStateException("Cannot assign signatories: dissolution data is not initialised");
        }
        this.data.setDirectors(directors);
    }

    public List<DissolutionDirector> getSignatories() {
        return this.data == null || this.data.getDirectors() == null
                ? Collections.emptyList()
                : this.data.getDirectors();
    }

    public String getSignatoryEmail(String signatoryId) {
        return this.getSignatories().stream()
                .filter(d -> d.getOfficerId().equals(signatoryId))
                .map(DissolutionDirector::getEmail)
                .findFirst()
                .orElseThrow(() -> new DissolutionSignatoryNotFoundException(
                        "No signatory found for signatory id " + signatoryId));
    }

    public String getFilingKind() {
        ApplicationType type = Optional.ofNullable(this.data)
                .map(DissolutionData::getApplication)
                .map(DissolutionApplication::getType)
                .orElseThrow(() -> new IllegalStateException("Dissolution does not have an application type"));
        return type == ApplicationType.LLDS01 ? FILING_KIND_LLDS01 : FILING_KIND_DS01;
    }

    public List<DissolutionStatusChanged> getStatusHistory() {
        return Collections.unmodifiableList(statusHistory);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    /**
     * Returns the reference number for this dissolution application, which is either:
     * the transaction ID (for transaction model dissolutions)
     * or the application reference from the dissolution data.
     */
    public String getReferenceNumber() {
        return isTransactionModelDissolution()
                ? this.getTransactionId()
                : this.getData().getApplication().getReference();
    }

    public boolean isTransactionModelDissolution() {
        return StringUtils.isNotBlank(this.getTransactionId());
    }

    /**
     * Returns the date/time the dissolution application was initiated, which is either:
     * the date/time the dissolution status was changed to PENDING (for transaction model
     * dissolutions, as the draft may have been created earlier than the application itself
     * was initiated), or the date/time the application was created otherwise.
     */
    public LocalDateTime dateDissolutionInitiated() {
        return isTransactionModelDissolution()
                ? this.getStatusHistory()
                .stream()
                .filter(statusChanged -> statusChanged.getStatus() == DissolutionStatus.PENDING)
                .map(DissolutionStatusChanged::getChangedAt)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Dissolution has not been initiated: no PENDING status found in status history"))
                : this.getCreatedBy().getDateTime();
    }
}
