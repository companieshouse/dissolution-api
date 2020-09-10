package uk.gov.companieshouse.mapper.email;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.fixtures.EmailFixtures;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.model.dto.email.MessageType;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.companieshouse.fixtures.EmailFixtures.EMAIL_ADDRESS;
import static uk.gov.companieshouse.fixtures.EmailFixtures.EMAIL_DATA;

public class EmailMapperTest {

    private static EmailDocument<?> emailDocument;

    private final EmailMapper emailMapper = new EmailMapper();

    @BeforeAll
    public static void setUp() {
        emailDocument = EmailFixtures.generateEmailDocument("some-email-data");
    }

    @Test
    public void mapToEmailDocument() {
        final EmailDocument<?> result = emailMapper.mapToEmailDocument(EMAIL_DATA, EMAIL_ADDRESS, MessageType.APPLICATION_ACCEPTED);

        assertEquals(emailDocument.getAppId(), result.getAppId());
        assertNotNull(result.getMessageId());
        assertEquals(emailDocument.getMessageType(), result.getMessageType());
        assertEquals(emailDocument.getData(), result.getData());
        assertEquals(emailDocument.getEmailAddress(), result.getEmailAddress());
        assertNotNull(result.getCreatedAt());
        assertEquals(emailDocument.getTopic(), result.getTopic());
    }

    @Test
    public void mapToKafkaMessage() {
        final byte[] serialisedEmailDocument = "some-serialised-email-document".getBytes();

        final Message message = EmailFixtures.generateKafkaMessage(serialisedEmailDocument);

        final Message result = emailMapper.mapToKafkaMessage(emailDocument, serialisedEmailDocument);

        assertEquals(message.getTopic(), result.getTopic());
        assertNotNull(message.getTimestamp());
        assertEquals(message.getValue(), result.getValue());
    }
}
