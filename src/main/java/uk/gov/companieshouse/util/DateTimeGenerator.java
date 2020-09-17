package uk.gov.companieshouse.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateTimeGenerator {

    private DateTimeGenerator () {}

    public static LocalDateTime generateCurrentDateTime() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
