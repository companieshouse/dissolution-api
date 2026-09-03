package uk.gov.companieshouse.model.db.dissolution;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
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

    @Nested
    class dateDissolutionInitiated {

        @Test
        void when_non_transaction_model_dissolution_then_return_createdBy_dateTime() {
            var createdByDateTime = LocalDateTime.now().minusDays(3);
            var createdBy = new CreatedBy();
            createdBy.setDateTime(createdByDateTime);

            var dissolution = new Dissolution();
            dissolution.setCreatedBy(createdBy);
            dissolution.changeStatus(DissolutionStatus.PENDING, LocalDateTime.now());

            assertThat(dissolution.dateDissolutionInitiated()).isEqualTo(createdByDateTime);
        }

        @Test
        void when_transaction_model_dissolution_then_return_changedAt_of_PENDING_status() {
            var draftCreatedAt = LocalDateTime.now().minusDays(5);
            var pendingChangedAt = LocalDateTime.now().minusDays(1);
            var createdBy = new CreatedBy();
            createdBy.setDateTime(draftCreatedAt);

            var dissolution = new Dissolution();
            dissolution.setCreatedBy(createdBy);
            dissolution.setTransactionId(TRANSACTION_ID);
            dissolution.changeStatus(DissolutionStatus.DRAFT, draftCreatedAt);
            dissolution.changeStatus(DissolutionStatus.PENDING, pendingChangedAt);

            assertThat(dissolution.dateDissolutionInitiated()).isEqualTo(pendingChangedAt);
        }

        @Test
        void when_transaction_model_dissolution_and_no_PENDING_status_in_history_then_exception_thrown() {
            var dissolution = new Dissolution();
            dissolution.setTransactionId(TRANSACTION_ID);
            dissolution.changeStatus(DissolutionStatus.DRAFT, LocalDateTime.now());

            assertThrows(IllegalStateException.class, dissolution::dateDissolutionInitiated);
        }
    }

    @Nested
    class assignSignatories {

        @Test
        void assigns_the_directors_to_the_dissolution_data() {
            var dissolution = new Dissolution();
            dissolution.setData(new DissolutionData());
            var directors = List.of(new DissolutionDirector());

            dissolution.assignSignatories(directors);

            assertThat(dissolution.getData().getDirectors()).isEqualTo(directors);
        }

        @Test
        void when_data_is_not_initialised_then_exception_thrown() {
            var dissolution = new Dissolution();
            var directors = List.of(new DissolutionDirector());

            assertThrows(IllegalStateException.class,
                    () -> dissolution.assignSignatories(directors));
        }
    }

    @Nested
    class getSignatories {

        @Test
        void when_data_is_not_initialised_then_returns_empty_list() {
            final var dissolution = new Dissolution();

            assertThat(dissolution.getSignatories()).isEmpty();
        }

        @Test
        void when_directors_is_null_then_returns_empty_list() {
            final var dissolution = aDissolution().withDirectors((List<DissolutionDirector>) null).build();

            assertThat(dissolution.getSignatories()).isEmpty();
        }

        @Test
        void when_directors_are_set_then_returns_them() {
            final var dissolution = aDissolution()
                    .withOnlyDirector(aDissolutionDirector().withOfficerId("abc123"))
                    .build();

            assertThat(dissolution.getSignatories())
                    .extracting(DissolutionDirector::getOfficerId)
                    .containsExactly("abc123");
        }
    }

    @Nested
    class findSignatory {

        @Test
        void when_data_is_not_initialised_then_empty_optional_returned() {
            final var dissolution = new Dissolution();
            assertThat(dissolution.findSignatory("abc123")).isEmpty();
        }

        @Test
        void when_no_signatory_matches_id_then_empty_optional_returned() {
            final var dissolution = aDissolution()
                    .withOnlyDirector(aDissolutionDirector().withOfficerId("some-other-id"))
                    .build();

            assertThat(dissolution.findSignatory("abc123")).isEmpty();
        }

        @Test
        void when_signatory_matches_id_then_returns_their_email() {
            final var dissolution = aDissolution()
                    .withOnlyDirector(aDissolutionDirector()
                            .withOfficerId("abc123")
                            .withEmail("john@doe.com"))
                    .build();

            assertThat(dissolution.findSignatory("abc123"))
                    .isPresent()
                    .get()
                    .extracting(DissolutionDirector::getEmail)
                    .isEqualTo("john@doe.com");
        }
    }

    @Nested
    class getApplicationType {

        @Test
        void when_data_is_not_initialised_then_exception_thrown() {
            var dissolution = new Dissolution();

            assertThrows(IllegalStateException.class, dissolution::getApplicationType);
        }

        @Test
        void when_application_is_not_initialised_then_exception_thrown() {
            var dissolution = aDissolution().build();
            dissolution.getData().setApplication(null);

            assertThrows(IllegalStateException.class, dissolution::getApplicationType);
        }

        @Test
        void when_application_type_is_null_then_exception_thrown() {
            var dissolution = aDissolution().build();
            dissolution.getData().getApplication().setType(null);

            assertThrows(IllegalStateException.class, dissolution::getApplicationType);
        }

        @Test
        void when_application_is_DS01_then_return_the_correct_filing_kind() {
            var dissolution = aDissolution().build();

            assertThat(dissolution.getApplicationType()).isEqualTo(ApplicationType.DS01);
        }

        @Test
        void when_application_is_LLDS01_then_return_the_correct_filing_kind() {
            var dissolution = aDissolution().build();
            dissolution.getData().getApplication().setType(ApplicationType.LLDS01);
            assertThat(dissolution.getApplicationType()).isEqualTo(ApplicationType.LLDS01);
        }
    }
}
