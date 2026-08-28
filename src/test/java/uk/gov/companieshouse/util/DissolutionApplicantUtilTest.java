package uk.gov.companieshouse.util;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateCreatedBy;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.util.DissolutionApplicantUtil.doesEmailBelongToApplicant;
import static uk.gov.companieshouse.util.DissolutionApplicantUtil.isApplicant;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DissolutionApplicantUtilTest {
    private static final String EMAIL = "director@email.com";
    private static final String DIFFERENT_EMAIL = "director2@email.com";
    private static final String USER_ID = "user123";
    private static final String NON_APPLICANT_USER_ID = "other456";

    @Test
    void when_email_belongs_to_applicant_then_returns_true() {
        Dissolution dissolution = aDissolution()
                .withCreatedByEmail(EMAIL)
                .build();
        assertTrue(doesEmailBelongToApplicant(EMAIL, dissolution));
    }

    @Test
    void when_email_does_not_belong_to_applicant_then_returns_false() {
        Dissolution dissolution = aDissolution()
                .withCreatedByEmail(EMAIL)
                .build();
        assertFalse(doesEmailBelongToApplicant(DIFFERENT_EMAIL, dissolution));
    }

    @Test
    void when_user_id_belongs_to_applicant_then_returns_true() {
        final var createdBy = generateCreatedBy();
        createdBy.setUserId(USER_ID);
        Dissolution dissolution = aDissolution()
                .withCreatedBy(createdBy)
                .build();
        assertTrue(isApplicant(USER_ID, dissolution));
    }

    @Test
    void when_user_id_does_not_belong_to_applicant_then_returns_false() {
        final var createdBy = generateCreatedBy();
        createdBy.setUserId(USER_ID);
        Dissolution dissolution = aDissolution()
                .withCreatedBy(createdBy)
                .build();
        assertFalse(isApplicant(NON_APPLICANT_USER_ID, dissolution));
    }

    @Test
    void when_created_by_is_null_then_returns_false() {
        final var createdBy = generateCreatedBy();
        createdBy.setUserId(USER_ID);
        Dissolution dissolution = aDissolution()
                .withCreatedBy(null)
                .build();
        assertFalse(isApplicant(NON_APPLICANT_USER_ID, dissolution));
    }
}
