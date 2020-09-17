package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReferenceGeneratorTest {

    private final ReferenceGenerator generator = new ReferenceGenerator();

    @Test
    public void generateApplicationReference_generatesASixAlphanumericCharacterUppercaseString() throws Exception {
        final String result = generator.generateApplicationReference();

        assertEquals(6, result.length());
        assertTrue(result.matches("^[A-Z0-9]*$"));
    }
}
