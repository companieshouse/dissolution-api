package uk.gov.companieshouse.util;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static uk.gov.companieshouse.util.EricHelper.getEmail;

public class EricHelperTest {

    @Test
    public void returnsEmail_whenOnlyEmailIsPresent() {
        final String header = "user@mail.com";
        assertEquals("user@mail.com", getEmail(header));
    }

    @Test
    public void returnsEmail_whenEmailAndForenameArePresent() {
        final String header = "user@mail.com;forename=firstName";
        assertEquals("user@mail.com", getEmail(header));
    }

    @Test
    public void returnsEmail_whenEmailAndForenameAndSurnameArePresent() {
        final String header = "user@mail.com;forename=firstName;surname=lastName";
        assertEquals("user@mail.com", getEmail(header));
    }
}
