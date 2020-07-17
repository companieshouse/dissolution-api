package uk.gov.companieshouse.model.db.dissolution;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;

import java.time.LocalDateTime;

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
}
