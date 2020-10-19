package uk.gov.companieshouse.model.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.model.enums.RefundStatus;

public class RefundResponse {
    @JsonProperty("refund_id")
    private String refundId;

    @JsonProperty("created_date_time")
    private String createdDateTime;

    private int amount;

    private RefundStatus status;

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

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public RefundStatus getStatus() {
        return status;
    }
}
