package uk.gov.companieshouse.model.domain;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;

import static java.util.Objects.requireNonNull;

/**
 * Intermediary object holding all the parameters required to create a draft dissolution.
 */
public record CreateDraftDissolutionCommand(
        Transaction transaction,
        CompanyProfile companyProfile,
        String userId,
        String ipAddress,
        String email) {

    public CreateDraftDissolutionCommand {
        requireNonNull(transaction, "transaction must not be null");
        requireNonNull(companyProfile, "companyProfile must not be null");
        requireNonNull(userId, "userId must not be null");
        requireNonNull(ipAddress, "ipAddress must not be null");
        requireNonNull(email, "email must not be null");
    }
}
