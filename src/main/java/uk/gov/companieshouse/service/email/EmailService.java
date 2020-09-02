package uk.gov.companieshouse.service.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.exception.EmailSendException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.email.EmailMapper;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

import java.util.concurrent.ExecutionException;

@Service
public class EmailService {

    private final EmailSerialiser emailSerialiser;
    private final EmailMapper emailMapper;
    private final CHKafkaProducer kafkaProducer;
    private final Logger logger;

    @Autowired
    public EmailService(
            EmailSerialiser emailSerialiser,
            EmailMapper emailMapper,
            CHKafkaProducer kafkaProducer,
            Logger logger
    ) {
        this.emailSerialiser = emailSerialiser;
        this.emailMapper = emailMapper;
        this.kafkaProducer = kafkaProducer;
        this.logger = logger;
    }

    public void sendMessage(EmailDocument<?> emailDocument) {
        try {
            byte[] serialisedEmailDocument = emailSerialiser.serialise(emailDocument);

            Message message = emailMapper.mapToKafkaMessage(emailDocument, serialisedEmailDocument);

            kafkaProducer.send(message);
        } catch (ExecutionException e) {
            logger.error("Error sending message to kafka", e);
            throw new EmailSendException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            logger.error("Error - thread interrupted", e);
            throw new EmailSendException(e.getMessage());
        }
    }
}

