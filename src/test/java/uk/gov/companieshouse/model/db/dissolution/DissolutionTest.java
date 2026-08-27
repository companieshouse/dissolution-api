package uk.gov.companieshouse.model.db.dissolution;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;

@DisplayNameGeneration(ReplaceUnderscores.class)
class DissolutionTest {

    @Nested
    class changeStatus {

        @Test
        void updates_the_status() {
            var dissolution = new Dissolution();

            dissolution.changeStatus(DissolutionStatus.PENDING, LocalDateTime.now());

            assertThat(dissolution.getStatus()).isEqualTo(DissolutionStatus.PENDING);
        }

        @Test
        void records_the_history_of_status_changes() {
            var dissolution = new Dissolution();
            var firstChange = LocalDateTime.now().minusDays(1);
            var secondChange = LocalDateTime.now();

            dissolution.changeStatus(DissolutionStatus.DRAFT, firstChange);
            dissolution.changeStatus(DissolutionStatus.PENDING, secondChange);

            var history = dissolution.getStatusHistory();
            assertThat(history).hasSize(2);
            assertThat(history.get(0).getStatus()).isEqualTo(DissolutionStatus.DRAFT);
            assertThat(history.get(0).getChangedAt()).isEqualTo(firstChange);
            assertThat(history.get(1).getStatus()).isEqualTo(DissolutionStatus.PENDING);
            assertThat(history.get(1).getChangedAt()).isEqualTo(secondChange);
        }

        @Test
        void when_new_status_is_SUBMITTED_then_submittedAt_is_set() {
            var dissolution = new Dissolution();
            var submittedAt = LocalDateTime.now();

            dissolution.changeStatus(DissolutionStatus.SUBMITTED, submittedAt);

            assertThat(dissolution.getSubmittedAt()).isEqualTo(submittedAt);
        }

        @Test
        void when_new_status_is_not_SUBMITTED_then_submittedAt_is_not_set() {
            var dissolution = new Dissolution();

            dissolution.changeStatus(DissolutionStatus.DRAFT, LocalDateTime.now());
            dissolution.changeStatus(DissolutionStatus.PENDING, LocalDateTime.now());

            assertThat(dissolution.getSubmittedAt()).isNull();
        }
    }

    @Nested
    class TransactionModelDissolution {

        @Test
        void when_transaction_model_dissolution_then_return_transaction_id_as_reference() {
            final var dissolution = aDissolution().withTransactionId(TRANSACTION_ID).build();
            assertThat(dissolution.getReferenceNumber()).isEqualTo(TRANSACTION_ID);
        }

        @Test
        void when_non_transaction_model_dissolution_then_return_application_reference_as_reference() {
            final var dissolution = aDissolution().withApplicationReference("some-reference").build();
            assertThat(dissolution.getReferenceNumber()).isEqualTo("some-reference");
        }
    }
}
