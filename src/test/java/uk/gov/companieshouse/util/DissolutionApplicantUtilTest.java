package uk.gov.companieshouse.util;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.util.DissolutionApplicantUtil.doesEmailBelongToApplicant;

public class DissolutionApplicantUtilTest {
    private static final String EMAIL = "director@email.com";
    private static final String DIFFERENT_EMAIL = "director2@email.com";

    @Test
    void returnsTrue_whenEmailBelongsToApplicant() {
        Dissolution dissolution = aDissolution()
                .withCreatedByEmail(EMAIL)
                .build();
        assertTrue(doesEmailBelongToApplicant(EMAIL, dissolution));
    }

    @Test
    void returnsFalse_whenEmailDoesNotBelongToApplicant() {
        Dissolution dissolution = aDissolution()
                .withCreatedByEmail(EMAIL)
                .build();
        assertFalse(doesEmailBelongToApplicant(DIFFERENT_EMAIL, dissolution));
    }
}
