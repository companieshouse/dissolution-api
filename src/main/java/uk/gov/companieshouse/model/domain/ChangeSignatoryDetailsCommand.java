package uk.gov.companieshouse.model.domain;

import static java.util.Objects.requireNonNull;

public record ChangeSignatoryDetailsCommand(String userId, String officerId, String officerEmail, String onBehalfName) {
    public ChangeSignatoryDetailsCommand {
        requireNonNull(userId, "userId must not be null");
        requireNonNull(officerId, "officerId must not be null");
        requireNonNull(officerEmail, "officerEmail must not be null");
    }
}
