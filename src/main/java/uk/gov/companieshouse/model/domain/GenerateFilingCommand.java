package uk.gov.companieshouse.model.domain;

import uk.gov.companieshouse.api.model.transaction.Transaction;

import static java.util.Objects.requireNonNull;

/**
 * Intermediary object holding all the parameters required to generate a dissolution filing.
 */
public record GenerateFilingCommand(
        Transaction transaction,
        String companyNumber,
        String transactionId) {

    public GenerateFilingCommand {
        requireNonNull(transaction, "transaction must not be null");
        requireNonNull(companyNumber, "companyNumber must not be null");
        requireNonNull(transactionId, "transactionId must not be null");
    }
}
