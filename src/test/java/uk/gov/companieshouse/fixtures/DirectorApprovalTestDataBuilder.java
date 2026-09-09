package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;

import java.time.LocalDateTime;

public class DirectorApprovalTestDataBuilder {

    private String userId = "user123";
    private String ipAddress = "192.168.0.2";
    private LocalDateTime dateTime = LocalDateTime.now();

    public static DirectorApprovalTestDataBuilder aDirectorApproval() {
        return new DirectorApprovalTestDataBuilder();
    }

    public DirectorApprovalTestDataBuilder withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public DirectorApprovalTestDataBuilder withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public DirectorApprovalTestDataBuilder withDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public DirectorApproval build() {
        final DirectorApproval approval = new DirectorApproval();
        approval.setUserId(userId);
        approval.setIpAddress(ipAddress);
        approval.setDateTime(dateTime);
        return approval;
    }
}
