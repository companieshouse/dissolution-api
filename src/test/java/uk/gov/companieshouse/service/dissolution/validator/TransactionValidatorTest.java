package uk.gov.companieshouse.service.dissolution.validator;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.InvalidTransactionStateException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.fixtures.TransactionResourceTestDataBuilder;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;
import static uk.gov.companieshouse.model.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.model.Constants.SUBMISSION_URI_PATTERN;

@DisplayNameGeneration(ReplaceUnderscores.class)
class TransactionValidatorTest {

    private static final String DISSOLUTION_ID = "sub-456";
    private static final String COMPANY_NUMBER = "12345678";

    @Test
    void when_transaction_is_null_then_null_pointer_exception_thrown() {
        assertThatThrownBy(() -> TransactionValidator.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void when_transaction_has_wrong_status_then_invalid_transaction_state_exception_thrown() {
        final var transaction = TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.OPEN).build();
        final var validator = TransactionValidator.of(transaction).hasStatus(TransactionStatus.CLOSED);

        assertThatThrownBy(validator::validate).isInstanceOf(InvalidTransactionStateException.class);
    }

    @Test
    void when_company_number_is_null_then_invalid_transaction_state_exception_thrown() {
        final var transaction = TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.OPEN).withCompanyNumber(null).build();
        final var validator = TransactionValidator.of(transaction).forCompany(COMPANY_NUMBER);

        assertThatThrownBy(validator::validate).isInstanceOf(InvalidTransactionStateException.class);
    }

    @Test
    void when_transaction_is_not_linked_to_company_then_invalid_transaction_state_exception_thrown() {
        final var transaction = TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.OPEN).withCompanyNumber("87654321").build();
        final var validator = TransactionValidator.of(transaction).forCompany(COMPANY_NUMBER);

        assertThatThrownBy(validator::validate).isInstanceOf(InvalidTransactionStateException.class);
    }

    @Test
    void when_dissolution_id_is_empty_then_dissolution_not_linked_to_transaction_exception_thrown() {
        final var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution("");

        assertThatThrownBy(validator::validate).isInstanceOf(DissolutionNotLinkedToTransactionException.class);
    }

    @Test
    void when_resources_is_null_then_dissolution_not_linked_to_transaction_exception_thrown() {
        final var transaction = TransactionFixtures.generateTransaction();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThatThrownBy(validator::validate).isInstanceOf(DissolutionNotLinkedToTransactionException.class);
    }

    @Test
    void when_resources_is_empty_then_dissolution_not_linked_to_transaction_exception_thrown() {
        final var transaction = TransactionTestDataBuilder.aTransaction().withResources(Map.of()).build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThatThrownBy(validator::validate).isInstanceOf(DissolutionNotLinkedToTransactionException.class);
    }

    @Test
    void when_resource_kind_is_not_a_dissolution_kind_then_dissolution_not_linked_to_transaction_exception_thrown() {
        var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource("some-other-kind", DISSOLUTION_ID))
                .build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThatThrownBy(validator::validate).isInstanceOf(DissolutionNotLinkedToTransactionException.class);
    }

    @Test
    void when_resource_has_dissolution_kind_but_link_does_not_match_then_dissolution_not_linked_to_transaction_exception_thrown() {
        final var resourceBuilder = TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID)
                .withSingleLink(LINK_RESOURCE, "/transactions/other-tx/dissolution/other-sub");
        final var transaction = TransactionTestDataBuilder.aTransaction().withResources(resourceBuilder).build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThatThrownBy(validator::validate).isInstanceOf(DissolutionNotLinkedToTransactionException.class);
    }

    @Test
    void when_resource_has_dissolution_kind_but_links_is_null_then_dissolution_not_linked_to_transaction_exception_thrown() {
        final var resource = TransactionResourceTestDataBuilder.aTransactionResource()
                .withResourceKey(String.format(SUBMISSION_URI_PATTERN, TRANSACTION_ID, DISSOLUTION_ID))
                .withKind(FILING_KIND_DS01)
                .withLinks((Map<String, String>) null);
        final var transaction = TransactionTestDataBuilder.aTransaction().withResources(resource).build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThatThrownBy(validator::validate).isInstanceOf(DissolutionNotLinkedToTransactionException.class);
    }

    @Test
    void when_ds01_resource_link_matches_submission_uri_then_no_exception_thrown() {
        final var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    void when_llds01_resource_link_matches_submission_uri_then_no_exception_thrown() {
        final var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, DISSOLUTION_ID))
                .build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    void when_one_of_multiple_resources_matches_then_no_exception_thrown() {
        final var nonMatchingResource = TransactionFixtures.generateTransactionResource("some-other-kind", DISSOLUTION_ID)
                .withResourceKey("other-resource-key")
                .withSingleLink(LINK_RESOURCE, "/some-other/link");
        final var matchingResource = TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, DISSOLUTION_ID);
        final var transaction = TransactionTestDataBuilder.aTransaction().withResources(nonMatchingResource, matchingResource).build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    void when_validating_multiple_properties_then_no_exception_thrown() {
        final var transaction = TransactionTestDataBuilder.aTransaction()
                .withStatus(TransactionStatus.OPEN)
                .withCompanyNumber(COMPANY_NUMBER)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();

        final var validator = TransactionValidator.of(transaction)
                .hasStatus(TransactionStatus.OPEN)
                .forCompany(COMPANY_NUMBER)
                .isLinkedToDissolution(DISSOLUTION_ID);

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }
}
