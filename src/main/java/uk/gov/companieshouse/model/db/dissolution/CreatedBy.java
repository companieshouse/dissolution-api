package uk.gov.companieshouse.model.db.dissolution;

import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

public class CreatedBy {

    @Field("user_id")
    private String userId;

    private String email;

    @Field("ip_address")
    private String ipAddress;

    @Field("date_time")
    private LocalDateTime dateTime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }
}
