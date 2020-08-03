package uk.gov.companieshouse.mapper.barcode;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.dto.barcode.BarcodeRequest;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class BarcodeMapperTest {

    final BarcodeMapper mapper = new BarcodeMapper();

    @Test
    public void mapToBarcodeRequest_setDateReceived() {
        LocalDateTime dateReceived = LocalDateTime.of(2020, 1, 1, 0, 0);

        final BarcodeRequest result = mapper.mapToBarcodeRequest(dateReceived);

        assertEquals(20200101, result.getDateReceived());
    }
}
