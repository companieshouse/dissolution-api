package uk.gov.companieshouse.mapper.email;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.model.dto.email.MessageType;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.util.UUIDGenerator;

import java.time.ZoneOffset;

import static uk.gov.companieshouse.model.Constants.EMAIL_APP_ID;
import static uk.gov.companieshouse.model.Constants.EMAIL_TOPIC;
import static uk.gov.companieshouse.util.DateTimeGenerator.generateCurrentDateTime;

@Service
public class EmailMapper {

    public <T> EmailDocument<T> mapToEmailDocument(T emailData, String emailAddress, MessageType messageType) {
        EmailDocument<T> emailDocument = new EmailDocument<>();

        emailDocument.setAppId(EMAIL_APP_ID);
        emailDocument.setMessageId(UUIDGenerator.generateUUID());
        emailDocument.setMessageType(messageType.getValue());
        emailDocument.setData(emailData);
        emailDocument.setEmailAddress(emailAddress);
        emailDocument.setCreatedAt(generateCurrentDateTime().toString());
        emailDocument.setTopic(EMAIL_TOPIC);

        return emailDocument;
    }

    public <T> Message mapToKafkaMessage(EmailDocument<T> emailDocument, byte[] serialisedEmailDocument) {
        Message message = new Message();

        message.setTopic(emailDocument.getTopic());
        message.setTimestamp(generateCurrentDateTime().toEpochSecond(ZoneOffset.UTC));
        message.setValue(serialisedEmailDocument);

        return message;
    }
}
