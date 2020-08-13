package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.enums.SubmissionStatus;

@Service
public class DissolutionSubmissionMapper {
    public DissolutionSubmission generateSubmissionInformation() {
        DissolutionSubmission submission = new DissolutionSubmission();

        submission.setStatus(SubmissionStatus.PENDING);
        submission.setRetryCounter(0);
        submission.setDateTime(null);

        return submission;
    }
}
