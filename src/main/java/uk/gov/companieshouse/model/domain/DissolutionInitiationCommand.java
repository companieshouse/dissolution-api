package uk.gov.companieshouse.model.domain;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Intermediary object holding all the parameters required to initiate a dissolution.
 */
public record DissolutionInitiationCommand(
        Transaction transaction,
        String companyNumber,
        String userId,
        List<DirectorRequest> signatories) {

    public DissolutionInitiationCommand {
        requireNonNull(transaction, "transaction must not be null");
        requireNonNull(companyNumber, "companyNumber must not be null");
        requireNonNull(userId, "userId must not be null");
        requireNonNull(signatories, "signatories must not be null");
        signatories = List.copyOf(signatories);
    }
}
