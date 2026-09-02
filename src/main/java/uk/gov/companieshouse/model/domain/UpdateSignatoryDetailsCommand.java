package uk.gov.companieshouse.model.domain;

import uk.gov.companieshouse.api.model.transaction.Transaction;

import static java.util.Objects.requireNonNull;

/**
 * Intermediary object holding all the parameters required to update the details of a signatory.
 */
public record UpdateSignatoryDetailsCommand(
        Transaction transaction,
        String companyNumber,
        String userId,
        String officerId,
        String officerEmail,
        String onBehalfName) {

    public UpdateSignatoryDetailsCommand {
        requireNonNull(transaction, "transaction must not be null");
        requireNonNull(companyNumber, "companyNumber must not be null");
        requireNonNull(userId, "userId must not be null");
        requireNonNull(officerId, "officerId must not be null");
        requireNonNull(officerEmail, "officerEmail must not be null");
    }
}
