package uk.gov.companieshouse.model.db.dissolution;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import java.time.LocalDateTime;

public class DissolutionStatusChanged {

    private DissolutionStatus status;

    @Field("changed_at")
    private LocalDateTime changedAt;

    public DissolutionStatusChanged() {
    }

    public DissolutionStatusChanged(DissolutionStatus status, LocalDateTime changedAt) {
        this.status = status;
        this.changedAt = changedAt;
    }

    public DissolutionStatus getStatus() {
        return status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
