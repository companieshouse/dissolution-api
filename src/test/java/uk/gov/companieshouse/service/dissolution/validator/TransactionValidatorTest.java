package uk.gov.companieshouse.service.dissolution.validator;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.InvalidTransactionStateException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;
import static uk.gov.companieshouse.model.Constants.LINK_RESOURCE;

class TransactionValidatorTest {

    private static final String DISSOLUTION_ID = "sub-456";
    private static final String COMPANY_NUMBER = "12345678";

    @Test
    void transactionValidator_throwsNullPointerException_whenTransactionIsNull() {
        assertThrows(NullPointerException.class, () -> TransactionValidator.of(null));
    }

    @Test
    void transactionValidator_throwsInvalidTransactionStateException_whenTransactionHasWrongStatus() {
        final var transaction = TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.OPEN).build();
        final var validator = TransactionValidator.of(transaction).hasStatus(TransactionStatus.CLOSED);

        assertThrows(InvalidTransactionStateException.class, validator::validate);
    }

    @Test
    void transactionValidator_throwsInvalidTransactionStateException_whenComanyNumberIsNull() {
        final var transaction = TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.OPEN).withCompanyNumber(null).build();
        final var validator = TransactionValidator.of(transaction).forCompany(COMPANY_NUMBER);

        assertThrows(InvalidTransactionStateException.class, validator::validate);
    }

    @Test
    void transactionValidator_throwsInvalidTransactionStateException_whenTransactionIsNotLinkedToCompany() {
        final var transaction = TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.OPEN).withCompanyNumber("87654321").build();
        final var validator = TransactionValidator.of(transaction).forCompany(COMPANY_NUMBER);

        assertThrows(InvalidTransactionStateException.class, validator::validate);
    }

    @Test
    void transactionValidator_throwsDissolutionNotLinkedToTransactionException_whenDissolutionIdIsEmpty() {
        final var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution("");

        assertThrows(DissolutionNotLinkedToTransactionException.class, validator::validate);
    }

    @Test
    void transactionValidator_throwsDissolutionNotLinkedToTransactionException_whenResourcesIsNull() {
        final var transaction = TransactionFixtures.generateTransaction();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThrows(DissolutionNotLinkedToTransactionException.class, validator::validate);
    }

    @Test
    void transactionValidator_throwsDissolutionNotLinkedToTransactionException_whenResourcesIsEmpty() {
        final var transaction = TransactionTestDataBuilder.aTransaction().withResources(Map.of()).build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThrows(DissolutionNotLinkedToTransactionException.class, validator::validate);
    }

    @Test
    void transactionValidator_throwsDissolutionNotLinkedToTransactionException_whenResourceKindIsNotADissolutionKind() {
        var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource("some-other-kind", DISSOLUTION_ID))
                .build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThrows(DissolutionNotLinkedToTransactionException.class, validator::validate);
    }

    @Test
    void transactionValidator_throwsDissolutionNotLinkedToTransactionException_whenResourceHasDissolutionKindButLinkDoesNotMatch() {
        final var resourceBuilder = TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID)
                .withSingleLink(LINK_RESOURCE, "/transactions/other-tx/dissolution/other-sub");
        final var transaction = TransactionTestDataBuilder.aTransaction().withResources(resourceBuilder).build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertThrows(DissolutionNotLinkedToTransactionException.class, validator::validate);
    }

    @Test
    void transactionValidator_doesNotThrow_whenDs01ResourceLinkMatchesSubmissionUri() {
        final var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void transactionValidator_doesNotThrow_whenLlds01ResourceLinkMatchesSubmissionUri() {
        final var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, DISSOLUTION_ID))
                .build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void transactionValidator_doesNotThrow_whenOneOfMultipleResourcesMatches() {
        final var nonMatchingResource = TransactionFixtures.generateTransactionResource("some-other-kind", DISSOLUTION_ID)
                .withResourceKey("other-resource-key")
                .withSingleLink(LINK_RESOURCE, "/some-other/link");
        final var matchingResource = TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, DISSOLUTION_ID);
        final var transaction = TransactionTestDataBuilder.aTransaction().withResources(nonMatchingResource, matchingResource).build();
        final var validator = TransactionValidator.of(transaction).isLinkedToDissolution(DISSOLUTION_ID);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void transactionValidator_doesNotThrow_whenValidatingMultipleProperties() {
        final var transaction = TransactionTestDataBuilder.aTransaction()
                .withStatus(TransactionStatus.OPEN)
                .withCompanyNumber(COMPANY_NUMBER)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();

        final var validator = TransactionValidator.of(transaction)
                .hasStatus(TransactionStatus.OPEN)
                .forCompany(COMPANY_NUMBER)
                .isLinkedToDissolution(DISSOLUTION_ID);

        assertDoesNotThrow(validator::validate);
    }
}
