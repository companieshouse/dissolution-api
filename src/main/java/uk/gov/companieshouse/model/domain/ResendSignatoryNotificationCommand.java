package uk.gov.companieshouse.model.domain;

import uk.gov.companieshouse.api.model.transaction.Transaction;

import static java.util.Objects.requireNonNull;

/**
 * Intermediary object holding all the parameters required to resend a signatory notification.
 */
public record ResendSignatoryNotificationCommand(
        Transaction transaction,
        String companyNumber,
        String signatoryId) {

    public ResendSignatoryNotificationCommand {
        requireNonNull(transaction, "transaction must not be null");
        requireNonNull(companyNumber, "companyNumber must not be null");
        requireNonNull(signatoryId, "signatoryId must not be null");
    }
}
