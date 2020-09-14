package uk.gov.companieshouse.model.db.dissolution;

import java.util.List;
import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.model.enums.VerdictResult;

import java.time.LocalDateTime;

public class DissolutionVerdict {

    private VerdictResult result;

    @Field("reject_reasons")
    private List<DissolutionRejectReason> rejectReasons;

    @Field("date_time")
    private LocalDateTime dateTime;

    public VerdictResult getResult() {
        return result;
    }

    public void setResult(VerdictResult result) {
        this.result = result;
    }

    public List<DissolutionRejectReason> getRejectReasons() {
        return rejectReasons;
    }

    public void setRejectReasons(List<DissolutionRejectReason> rejectReasons) {
        this.rejectReasons = rejectReasons;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
