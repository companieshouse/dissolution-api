package uk.gov.companieshouse.model.db.payment;

import org.springframework.data.mongodb.core.mapping.Field;

public class RefundInformation {
    @Field("refund_id")
    private String refundId;

    @Field("created_date_time")
    private String createdDateTime;

    private int amount;

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setCreatedDateTime(String createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public String getCreatedDateTime() {
        return createdDateTime;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
