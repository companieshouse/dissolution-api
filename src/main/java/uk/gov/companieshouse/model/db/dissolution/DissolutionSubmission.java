package uk.gov.companieshouse.model.db.dissolution;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.model.enums.SubmissionStatus;

import java.time.LocalDateTime;

public class DissolutionSubmission {

    private SubmissionStatus status;

    @Field("retry_counter")
    private int retryCounter;

    @Field("date_time")
    private LocalDateTime dateTime;

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public int getRetryCounter() {
        return retryCounter;
    }

    public void setRetryCounter(int retryCounter) {
        this.retryCounter = retryCounter;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
