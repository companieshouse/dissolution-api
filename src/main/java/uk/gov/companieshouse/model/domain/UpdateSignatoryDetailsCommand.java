package uk.gov.companieshouse.model.domain;

import static java.util.Objects.requireNonNull;

public record UpdateSignatoryDetailsCommand(String userId, String officerId, String officerEmail, String onBehalfName) {
    public UpdateSignatoryDetailsCommand {
        requireNonNull(userId, "userId must not be null");
        requireNonNull(officerId, "officerId must not be null");
        requireNonNull(officerEmail, "officerEmail must not be null");
    }
}
