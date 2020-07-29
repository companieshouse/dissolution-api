package uk.gov.companieshouse.model.dto.dissolution;


import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;

import java.sql.Timestamp;
import java.util.List;

public class DissolutionGetResponse {
    @JsonProperty("ETag")
    private String ETag;

    private String kind;

    private DissolutionLinks links;

    @JsonProperty("application_status")
    private ApplicationStatus applicationStatus;

    @JsonProperty("application_reference")
    private String applicationReference;

    @JsonProperty("application_type")
    private ApplicationType applicationType;

    @JsonProperty("company_name")
    private String companyName;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("certificate_bucket")
    private String certificateBucket;

    @JsonProperty("certificate_key")
    private String certificateKey;

    private List<DissolutionGetDirector> directors;

    @JsonProperty("ETag")
    public String getETag() {
        return ETag;
    }

    public void setETag(String ETag) {
        this.ETag = ETag;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public DissolutionLinks getLinks() {
        return links;
    }

    public void setLinks(DissolutionLinks links) {
        this.links = links;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getApplicationReference() {
        return applicationReference;
    }

    public void setApplicationReference(String applicationReference) {
        this.applicationReference = applicationReference;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<DissolutionGetDirector> getDirectors() {
        return directors;
    }

    public void setDirectors(List<DissolutionGetDirector> directors) {
        this.directors = directors;
    }

    public String getCertificateBucket() {
        return certificateBucket;
    }

    public void setCertificateBucket(String certificateBucket) {
        this.certificateBucket = certificateBucket;
    }

    public String getCertificateKey() {
        return certificateKey;
    }

    public void setCertificateKey(String certificateKey) {
        this.certificateKey = certificateKey;
    }
}
