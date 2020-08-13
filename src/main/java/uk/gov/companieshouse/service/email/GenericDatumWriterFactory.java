package uk.gov.companieshouse.service.email;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;

@Component
public class GenericDatumWriterFactory {
    public GenericDatumWriter<GenericRecord> getGenericDatumWriter(Schema schema) {
        return new GenericDatumWriter<>(schema);
    }
}
