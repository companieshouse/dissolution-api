package uk.gov.companieshouse.service.transaction;

import static java.util.Objects.requireNonNull;

/**
 * Holds the dissolution data required to configure a transaction with the dissolution filing resource.
 */
public record DissolutionTransactionConfig(String companyNumber, String filingKind, String companyName) {

    public DissolutionTransactionConfig {
        requireNonNull(companyNumber, "companyNumber must not be null");
        requireNonNull(filingKind, "filingKind must not be null");
        requireNonNull(companyName, "companyName must not be null");
    }
}
