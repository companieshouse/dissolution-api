package uk.gov.companieshouse.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.exception.EmailSendException;
import uk.gov.companieshouse.fixtures.EmailFixtures;
import uk.gov.companieshouse.kafka.ChdKafkaProducer;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.email.EmailMapper;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private EmailSerialiser emailSerialiser;

    @Mock
    private EmailMapper emailMapper;

    @Mock
    private ChdKafkaProducer kafkaProducer;

    @Mock
    private Logger logger;

    @Test
    public void sendMessage() throws ExecutionException, InterruptedException {
        final EmailDocument<?> emailDocument = EmailFixtures.generateEmailDocument("some-email-data");

        final byte[] serialisedEmailDocument = "some-serialised-email-document".getBytes();

        when(emailSerialiser.serialise(emailDocument)).thenReturn(serialisedEmailDocument);

        final Message message = EmailFixtures.generateKafkaMessage(serialisedEmailDocument);

        when(emailMapper.mapToKafkaMessage(emailDocument, serialisedEmailDocument)).thenReturn(message);

        emailService.sendMessage(emailDocument);

        verify(kafkaProducer).send(message);
    }

    @Test
    public void executionExceptionOccursWhenAttemptingToSendMessage() throws ExecutionException, InterruptedException {
        final EmailDocument<?> emailDocument = EmailFixtures.generateEmailDocument("some-email-data");

        final byte[] serialisedEmailDocument = "some-serialised-email-document".getBytes();

        when(emailSerialiser.serialise(emailDocument)).thenReturn(serialisedEmailDocument);

        final Message message = EmailFixtures.generateKafkaMessage(serialisedEmailDocument);

        when(emailMapper.mapToKafkaMessage(emailDocument, serialisedEmailDocument)).thenReturn(message);

        doThrow(ExecutionException.class).when(kafkaProducer).send(message);

        Executable actual = () -> emailService.sendMessage(emailDocument);

        assertThrows(EmailSendException.class, actual);
    }

    @Test
    public void interruptedExceptionOccursWhenAttemptingToSendMessage() throws ExecutionException, InterruptedException {
        final EmailDocument<?> emailDocument = EmailFixtures.generateEmailDocument("some-email-data");

        final byte[] serialisedEmailDocument = "some-serialised-email-document".getBytes();

        when(emailSerialiser.serialise(emailDocument)).thenReturn(serialisedEmailDocument);

        final Message message = EmailFixtures.generateKafkaMessage(serialisedEmailDocument);

        when(emailMapper.mapToKafkaMessage(emailDocument, serialisedEmailDocument)).thenReturn(message);

        doThrow(InterruptedException.class).when(kafkaProducer).send(message);

        Executable actual = () -> emailService.sendMessage(emailDocument);

        assertThrows(EmailSendException.class, actual);
    }
}
