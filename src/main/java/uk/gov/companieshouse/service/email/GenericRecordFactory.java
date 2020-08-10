package uk.gov.companieshouse.service.email;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;

@Component
public class GenericRecordFactory {
    public GenericRecord getGenericRecord(Schema schema) {
        return new GenericData.Record(schema);
    }
}
