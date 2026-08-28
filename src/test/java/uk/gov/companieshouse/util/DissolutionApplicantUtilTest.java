package uk.gov.companieshouse.util;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateCreatedBy;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.util.DissolutionApplicantUtil.doesEmailBelongToApplicant;
import static uk.gov.companieshouse.util.DissolutionApplicantUtil.isApplicant;

class DissolutionApplicantUtilTest {
    private static final String EMAIL = "director@email.com";
    private static final String DIFFERENT_EMAIL = "director2@email.com";
    private static final String USER_ID = "user123";
    private static final String NON_APPLICANT_USER_ID = "other456";


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

    @Test
    void returnsTrue_whenUserIdBelongsToApplicant() {
        final var createdBy = generateCreatedBy();
        createdBy.setUserId(USER_ID);
        Dissolution dissolution = aDissolution()
                .withCreatedBy(createdBy)
                .build();
        assertTrue(isApplicant(USER_ID, dissolution));
    }

    @Test
    void returnsFalse_whenUserIdDoesNotBelongToApplicant() {
        final var createdBy = generateCreatedBy();
        createdBy.setUserId(USER_ID);
        Dissolution dissolution = aDissolution()
                .withCreatedBy(createdBy)
                .build();
        assertFalse(isApplicant(NON_APPLICANT_USER_ID, dissolution));
    }
}
