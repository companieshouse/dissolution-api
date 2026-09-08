package uk.gov.companieshouse.service.dissolution.validator;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static uk.gov.companieshouse.fixtures.DirectorApprovalTestDataBuilder.aDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;

@DisplayNameGeneration(ReplaceUnderscores.class)
class FilingValidatorTest {

    private static final String DISSOLUTION_ID = "12345678";

    private final FilingValidator filingValidator = new FilingValidator();

    @Test
    void when_dissolution_is_valid_then_valid_result() {
        final var dissolution = aDissolution().withId(DISSOLUTION_ID)
                .withOnlyDirector(aDissolutionDirector().withDirectorApproval(aDirectorApproval()))
                .build();
        dissolution.changeStatus(DissolutionStatus.SUBMITTED, LocalDateTime.now());

        final var result = filingValidator.validate(dissolution);

        assertThat(result.isValid()).isTrue();
    }

    @ParameterizedTest(name = "{index} => {0}")
    @EnumSource(value = DissolutionStatus.class, names = "SUBMITTED", mode = EnumSource.Mode.EXCLUDE)
    void when_dissolution_status_is_not_SUBMITTED_then_invalid_result_with_error(DissolutionStatus status) {
        final var dissolution = aDissolution().withId(DISSOLUTION_ID)
                .withOnlyDirector(aDissolutionDirector().withDirectorApproval(aDirectorApproval()))
                .build();
        dissolution.changeStatus(status, LocalDateTime.now());

        final var result = filingValidator.validate(dissolution);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly(
                String.format("Dissolution status is %s, expected SUBMITTED", status));
    }

    @Test
    void when_a_signatory_has_not_signed_then_invalid_result_with_error() {
        final var dissolution = aDissolution().withId(DISSOLUTION_ID)
                .withDirectors(
                        aDissolutionDirector().withOfficerId("abc123").withDirectorApproval(aDirectorApproval()),
                        aDissolutionDirector().withOfficerId("def456"))
                .build();
        dissolution.changeStatus(DissolutionStatus.SUBMITTED, LocalDateTime.now());

        final var result = filingValidator.validate(dissolution);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).containsExactly("Not all signatories have signed");
    }
}

