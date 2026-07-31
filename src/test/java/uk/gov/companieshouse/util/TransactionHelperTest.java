package uk.gov.companieshouse.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.fixtures.TransactionFixtures;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.buildResource;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.buildTransaction;
import static uk.gov.companieshouse.model.Constants.*;

class TransactionHelperTest {

    private static final String TRANSACTION_ID = TransactionFixtures.TRANSACTION_ID;
    private static final String DISSOLUTION_ID = "sub-456";
    private static final String DISSOLUTION_SELF_LINK =
            String.format(SUBMISSION_URI_PATTERN, TRANSACTION_ID, DISSOLUTION_ID);

    private static final String DISSOLUTION_RESOURCE_KEY =
            String.format("/transactions/%s/dissolution/%s", TRANSACTION_ID, DISSOLUTION_ID);

    private TransactionHelper helper;

    @BeforeEach
    void setup() {
        helper = new TransactionHelper();
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenTransactionIsNull() {
        assertFalse(helper.isTransactionLinkedToDissolution(null, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenDissolutionIdIsEmpty() {
        var resource = buildResource(FILING_KIND_DS01, DISSOLUTION_SELF_LINK);
        var transaction = buildTransaction(Map.of("resource-key", resource));
        assertFalse(helper.isTransactionLinkedToDissolution(transaction, ""));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenResourcesIsNull() {
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        assertFalse(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenResourcesIsEmpty() {
        var transaction = buildTransaction(Map.of());

        assertFalse(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenResourceKindIsNotADissolutionKind() {
        var resource = buildResource("some-other-kind", DISSOLUTION_SELF_LINK);
        var transaction = buildTransaction(Map.of(DISSOLUTION_RESOURCE_KEY, resource));

        assertFalse(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenResourceHasDissolutionKindButLinkDoesNotMatch() {
        var resource = buildResource(FILING_KIND_DS01, "/transactions/other-tx/dissolution/other-sub");
        var transaction = buildTransaction(Map.of(DISSOLUTION_RESOURCE_KEY, resource));

        assertFalse(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsTrue_whenDs01ResourceLinkMatchesSubmissionUri() {
        var resource = buildResource(FILING_KIND_DS01, DISSOLUTION_SELF_LINK);
        var transaction = buildTransaction(Map.of(DISSOLUTION_RESOURCE_KEY, resource));

        assertTrue(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsTrue_whenLlds01ResourceLinkMatchesSubmissionUri() {
        var resource = buildResource(FILING_KIND_LLDS01, DISSOLUTION_SELF_LINK);
        var transaction = buildTransaction(Map.of(DISSOLUTION_RESOURCE_KEY, resource));

        assertTrue(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsTrue_whenOneOfMultipleResourcesMatches() {
        var nonMatchingResource = buildResource("some-other-kind", "/wrong/link");
        var matchingResource = buildResource(FILING_KIND_DS01, DISSOLUTION_SELF_LINK);
        var transaction = buildTransaction(Map.of(
                "other-resource-key", nonMatchingResource,
                DISSOLUTION_RESOURCE_KEY, matchingResource
        ));

        assertTrue(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }
}