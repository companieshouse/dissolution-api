package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.db.dissolution.CreatedBy;

import java.time.LocalDateTime;

public class CreatedByTestDataBuilder {

    private String userId = "user123";
    private String email = "user@companieshouse.gov.uk";
    private String ipAddress = "192.168.0.2";
    private LocalDateTime dateTime = LocalDateTime.now();

    public static CreatedByTestDataBuilder aCreatedBy() {
        return new CreatedByTestDataBuilder();
    }

    public CreatedByTestDataBuilder withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public CreatedByTestDataBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public CreatedByTestDataBuilder withIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public CreatedByTestDataBuilder withDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public CreatedBy build() {
        final CreatedBy createdBy = new CreatedBy();
        createdBy.setUserId(userId);
        createdBy.setEmail(email);
        createdBy.setIpAddress(ipAddress);
        createdBy.setDateTime(dateTime);
        return createdBy;
    }
}
