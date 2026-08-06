package uk.gov.companieshouse.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;
import static uk.gov.companieshouse.model.Constants.LINK_RESOURCE;

class TransactionHelperTest {

    private static final String DISSOLUTION_ID = "sub-456";

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
        var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();
        assertFalse(helper.isTransactionLinkedToDissolution(transaction, ""));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenResourcesIsNull() {
        var transaction = TransactionFixtures.generateTransaction();
        assertFalse(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenResourcesIsEmpty() {
        var transaction = TransactionTestDataBuilder.aTransaction().withResources(Map.of()).build();
        assertFalse(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenResourceKindIsNotADissolutionKind() {
        var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource("some-other-kind", DISSOLUTION_ID))
                .build();
        assertFalse(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsFalse_whenResourceHasDissolutionKindButLinkDoesNotMatch() {
        var resourceBuilder = TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID)
                .withSingleLink(LINK_RESOURCE, "/transactions/other-tx/dissolution/other-sub");
        var transaction = TransactionTestDataBuilder.aTransaction().withResources(resourceBuilder).build();
        assertFalse(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsTrue_whenDs01ResourceLinkMatchesSubmissionUri() {
        var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();
        assertTrue(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsTrue_whenLlds01ResourceLinkMatchesSubmissionUri() {
        var transaction = TransactionTestDataBuilder.aTransaction()
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, DISSOLUTION_ID))
                .build();
        assertTrue(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }

    @Test
    void isTransactionLinkedToDissolution_returnsTrue_whenOneOfMultipleResourcesMatches() {
        var nonMatchingResource = TransactionFixtures.generateTransactionResource("some-other-kind", DISSOLUTION_ID)
                .withResourceKey("other-resource-key")
                .withSingleLink(LINK_RESOURCE, "/some-other/link");
        var matchingResource = TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, DISSOLUTION_ID);
        var transaction = TransactionTestDataBuilder.aTransaction().withResources(nonMatchingResource, matchingResource).build();

        assertTrue(helper.isTransactionLinkedToDissolution(transaction, DISSOLUTION_ID));
    }
}