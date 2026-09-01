package uk.gov.companieshouse.service.transaction;

import static java.util.Objects.requireNonNull;

/**
 * Intermediary object holding all the parameters required to update a transaction with the dissolution filing resource.
 */
public record TransactionFiling(String id, String kind, String companyName) {

    public TransactionFiling {
        requireNonNull(id, "filing id must not be null");
        requireNonNull(kind, "filing kind must not be null");
        requireNonNull(companyName, "companyName must not be null");
    }
}
