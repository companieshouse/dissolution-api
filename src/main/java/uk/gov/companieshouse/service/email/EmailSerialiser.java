package uk.gov.companieshouse.service.email;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.exception.EmailSendException;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class EmailSerialiser {

    private final GenericDatumWriterFactory datumWriterFactory;
    private final EncoderFactory encoderFactory;
    private final ObjectMapper mapper;
    private final GenericRecordFactory genericRecordFactory;
    private final Schema schema;

    private final Logger logger = LoggerFactory.getLogger(EmailSerialiser.class);

    @Autowired
    public EmailSerialiser(
            EncoderFactory encoderFactory, GenericDatumWriterFactory datumWriterFactory,
            GenericRecordFactory genericRecordFactory, ObjectMapper mapper, Schema schema
    ) {
        this.encoderFactory = encoderFactory;
        this.datumWriterFactory = datumWriterFactory;
        this.genericRecordFactory = genericRecordFactory;
        this.mapper = mapper;
        this.schema = schema;
    }

    public byte[] serialise(EmailDocument<?> document) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            BinaryEncoder encoder = encoderFactory.binaryEncoder(stream, null);

            GenericDatumWriter<GenericRecord> datumWriter = datumWriterFactory.getGenericDatumWriter(schema);
            GenericRecord avroGenericRecord = buildAvroGenericRecord(document, schema);
            datumWriter.write(avroGenericRecord, encoder);

            encoder.flush();

            return stream.toByteArray();
        } catch (IOException e) {
            logger.error("Error serialising email", e);
            throw new EmailSendException(e.getMessage());
        }
    }

    private GenericRecord buildAvroGenericRecord(
            EmailDocument<?> document, Schema schema
    ) throws JsonProcessingException {
        GenericRecord documentData = genericRecordFactory.getGenericRecord(schema);

        documentData.put("app_id", document.getAppId());
        documentData.put("message_id", document.getMessageId());
        documentData.put("message_type", document.getMessageType());
        documentData.put("data", mapper.writeValueAsString(document.getData()));
        documentData.put("email_address", document.getEmailAddress());
        documentData.put("created_at", document.getCreatedAt());

        return documentData;
    }
}
