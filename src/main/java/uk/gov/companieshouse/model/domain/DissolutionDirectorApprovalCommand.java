package uk.gov.companieshouse.model.domain;

import static java.util.Objects.requireNonNull;

public record DissolutionDirectorApprovalCommand(String userId, String officerId, String ipAddress, boolean hasApproved) {
    public DissolutionDirectorApprovalCommand {
        requireNonNull(userId, "userId must not be null");
        requireNonNull(officerId, "officerId must not be null");
        requireNonNull(ipAddress, "ipAddress must not be null");
    }
}
