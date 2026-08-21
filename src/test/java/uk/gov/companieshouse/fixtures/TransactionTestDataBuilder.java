package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.api.model.transaction.Filing;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionLinks;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionTestDataBuilder {

    private String id = TransactionFixtures.TRANSACTION_ID;
    private TransactionStatus status = TransactionStatus.OPEN;
    private Map<String, Resource> resources;
    private Map<String, Filing> filings;
    private TransactionLinks transactionLinks = new TransactionLinks();

    public static TransactionTestDataBuilder aTransaction() {
        return new TransactionTestDataBuilder();
    }

    public TransactionTestDataBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public TransactionTestDataBuilder withStatus(TransactionStatus status) {
        this.status = status;
        return this;
    }

    public TransactionTestDataBuilder withResources(TransactionResourceTestDataBuilder... resourceBuilders) {
        return withResources(Arrays.stream(resourceBuilders)
                .map(TransactionResourceTestDataBuilder::build)
                .collect(Collectors.toMap(
                        TransactionResourceTestDataBuilder.ResourceWithKey::key,
                        TransactionResourceTestDataBuilder.ResourceWithKey::resource)));
    }

    public TransactionTestDataBuilder withResources(Map<String, Resource> resources) {
        this.resources = new HashMap<>();
        this.resources.putAll(resources);
        return this;
    }

    public TransactionTestDataBuilder withPaymentLink(String paymentUri) {
        if (transactionLinks == null) {
            transactionLinks = new TransactionLinks();
        }
        transactionLinks.setPayment(paymentUri);
        return this;
    }

    public TransactionTestDataBuilder withFilings(Map<String, Filing> filings) {
        this.filings = filings;
        return this;
    }

    public TransactionTestDataBuilder withNoFilings() {
        return withFilings(Map.of());
    }

    public TransactionTestDataBuilder withFiling(FilingTestDataBuilder filingBuilder) {
        return withFiling("a-filing-id", filingBuilder);
    }

    public TransactionTestDataBuilder withFiling(String key, FilingTestDataBuilder filingBuilder) {
        if (filings == null) {
            filings = new HashMap<>();
        }
        filings.put(key, filingBuilder.build());
        return this;
    }

    public Transaction build() {
        final Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setStatus(status);
        transaction.setResources(resources);
        transaction.setFilings(filings);
        transaction.setLinks(transactionLinks);
        return transaction;
    }
}

