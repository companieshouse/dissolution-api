package uk.gov.companieshouse.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.EmailClient;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

@Service
public class EmailService {

    private final EmailClient emailClient;
    private final Logger logger;

    @Autowired
    public EmailService(EmailClient emailClient, Logger logger) {
        this.emailClient = emailClient;
        this.logger = logger;
    }

    public <T> void sendMessage(final EmailDocument<T> emailDocument) {
        logger.info("Sending email document to CHS Kafka API...");

        emailClient.sendEmail(emailDocument);
    }
}

