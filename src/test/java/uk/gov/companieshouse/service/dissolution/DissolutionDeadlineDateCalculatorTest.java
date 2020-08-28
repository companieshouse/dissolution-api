package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DissolutionDeadlineDateCalculatorTest {

    final DissolutionDeadlineDateCalculator calculator = new DissolutionDeadlineDateCalculator();

    @Test
    public void calculateSignatoryDeadlineDate_shouldReturnADateTwoWeeksAfterTheProvidedDate_andFormatIt() {
        final LocalDateTime startDate = LocalDateTime.of(2020, 9, 3, 0, 0);

        final String result = calculator.calculateSignatoryDeadlineDate(startDate);

        assertEquals("17 September 2020", result);
    }
}
