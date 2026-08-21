package uk.gov.companieshouse.fixtures;

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
    private String comanyNumber = "12345678";
    private Map<String, Resource> resources;
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

    public TransactionTestDataBuilder withCompanyNumber(String companyNumber) {
        this.comanyNumber = companyNumber;
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

    public Transaction build() {
        final Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setStatus(status);
        transaction.setCompanyNumber(comanyNumber);
        transaction.setResources(resources);
        transaction.setLinks(transactionLinks);
        return transaction;
    }
}

