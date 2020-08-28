package uk.gov.companieshouse.service.dissolution;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class DissolutionDeadlineDateCalculator {

    private static final String SIGNATORY_TO_SIGN_DATE_FORMAT = "dd MMMM yyyy";

    public String calculateSignatoryDeadlineDate(LocalDateTime start) {
        return start.plus(2, ChronoUnit.WEEKS).format(DateTimeFormatter.ofPattern(SIGNATORY_TO_SIGN_DATE_FORMAT));
    }
}
