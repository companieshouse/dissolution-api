package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.enums.SubmissionStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DissolutionSubmissionMapperTest {

    private final DissolutionSubmissionMapper mapper = new DissolutionSubmissionMapper();

    @Test
    public void generateSubmissionInformation_getsDissolutionSubmission() {
        final DissolutionSubmission submission = mapper.generateSubmissionInformation();

        assertNull(submission.getDateTime());
        assertEquals(0, submission.getRetryCounter());
        assertEquals(SubmissionStatus.PENDING, submission.getStatus());
    }
}
