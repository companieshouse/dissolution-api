package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SuccessfulPaymentEmailConfig extends EmailConfig {

    @Value("${email.successfulPayment.subject}")
    private String subject;

    @Value("${email.successfulPayment.messageType}")
    private String messageType;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
