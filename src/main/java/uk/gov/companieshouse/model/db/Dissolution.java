package uk.gov.companieshouse.model.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "dissolutions")
public class Dissolution {

    @Id
    private String id;

    private LocalDateTime modifiedDateTime;

    private DissolutionData data;

    private Company company;

    private CreatedBy createdBy;

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
}
