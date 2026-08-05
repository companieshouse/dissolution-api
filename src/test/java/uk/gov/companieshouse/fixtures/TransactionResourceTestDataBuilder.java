package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.api.model.transaction.Resource;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;

public class TransactionResourceTestDataBuilder {

    public record ResourceWithKey(String key, Resource resource) {
    }

    public record Link(String id, String uri) {
    }

    private String resourceKey = "";
    private String kind = FILING_KIND_DS01;
    private Map<String, String> links = Map.of();

    public static TransactionResourceTestDataBuilder aTransactionResource() {
        return new TransactionResourceTestDataBuilder();
    }

    public TransactionResourceTestDataBuilder withResourceKey(String key) {
        this.resourceKey = key;
        return this;
    }

    public TransactionResourceTestDataBuilder withKind(String kind) {
        this.kind = kind;
        return this;
    }

    public TransactionResourceTestDataBuilder withLinks(Link... links) {
        return withLinks(Arrays.stream(links).collect(Collectors.toMap(Link::id, Link::uri)));
    }

    public TransactionResourceTestDataBuilder withLinks(Map<String, String> links) {
        this.links = links;
        return this;
    }

    public TransactionResourceTestDataBuilder withSingleLink(String id, String uri) {
        this.links = Map.of(id, uri);
        return this;
    }

    public ResourceWithKey build() {
        final var resource = new Resource();
        resource.setKind(kind);
        resource.setLinks(links);
        return new ResourceWithKey(resourceKey, resource);
    }
}

