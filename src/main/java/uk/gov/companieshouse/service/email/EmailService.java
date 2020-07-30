package uk.gov.companieshouse.service.email;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutionException;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.exception.InternalServerErrorException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

@Service
public class EmailService {
    private final EmailSerialiser emailSerializer;
    private final Schema schema;
    private final CHKafkaProducer kafkaProducer;

    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    public EmailService(
        EmailSerialiser emailSerializer, Schema schema, CHKafkaProducer kafkaProducer
    ) {
        this.emailSerializer = emailSerializer;
        this.schema = schema;
        this.kafkaProducer = kafkaProducer;
    }

    public void sendMessage(EmailDocument<?> emailDocument) {
        Message message = new Message();
        message.setTopic(emailDocument.getTopic());
        message.setTimestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));

        try {
            message.setValue(emailSerializer.serialize(emailDocument, schema));
            kafkaProducer.send(message);
        } catch (ExecutionException e) {
            logger.error("Error sending message to kafka", e);
            throw new InternalServerErrorException("Error sending message to kafka");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Error - thread interrupted", e);
            throw new InternalServerErrorException("Error - thread interrupted");
        }
    }
}

