package uk.gov.companieshouse.service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.exception.EmailSendException;
import uk.gov.companieshouse.fixtures.EmailFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailSerialiserTest {

    private static EmailDocument<?> emailDocument;

    @InjectMocks
    private EmailSerialiser emailSerialiser;

    @Mock
    private EncoderFactory encoderFactory;

    @Mock
    private GenericDatumWriterFactory datumWriterFactory;

    @Mock
    private GenericRecordFactory genericRecordFactory;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Schema schema;

    @Mock
    private GenericDatumWriter<GenericRecord> datumWriter;

    @Mock
    private BinaryEncoder binaryEncoder;

    @Mock
    private GenericRecord genericRecord;

    @Mock
    private Logger logger;

    @BeforeAll
    public static void setUp() {
        emailDocument = EmailFixtures.generateEmailDocument("some-email-data");
    }

    @Test
    public void serialiseEmailDocumentSuccessfully() throws IOException {
        when(encoderFactory.binaryEncoder(any(), any())).thenReturn(binaryEncoder);
        when(datumWriterFactory.getGenericDatumWriter(any())).thenReturn(datumWriter);
        when(genericRecordFactory.getGenericRecord(schema)).thenReturn(genericRecord);
        when(mapper.writeValueAsString(emailDocument.getData())).thenReturn("some-email-data-as-string");

        byte[] actual = emailSerialiser.serialise(emailDocument);

        assertNotNull(actual);

        verify(genericRecord).put("app_id", emailDocument.getAppId());
        verify(genericRecord).put("message_id", emailDocument.getMessageId());
        verify(genericRecord).put("message_type", emailDocument.getMessageType());
        verify(genericRecord).put("data", "some-email-data-as-string");
        verify(genericRecord).put("email_address", emailDocument.getEmailAddress());
        verify(genericRecord).put("created_at", emailDocument.getCreatedAt());
    }

    @Test
    public void exceptionThrownWhenAttemptingToSerialiseEmailDocument() throws IOException {
        when(encoderFactory.binaryEncoder(any(), any())).thenReturn(binaryEncoder);
        when(datumWriterFactory.getGenericDatumWriter(any())).thenReturn(datumWriter);
        when(genericRecordFactory.getGenericRecord(schema)).thenReturn(genericRecord);
        when(mapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        Executable actual = () -> emailSerialiser.serialise(emailDocument);

        assertThrows(EmailSendException.class, actual);
    }
}
