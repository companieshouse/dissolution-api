package uk.gov.companieshouse.mapper.barcode;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.barcode.BarcodeRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BarcodeMapper {

    private static final String DATE_RECEIVED_PATTERN = "yyyyMMdd";

    public BarcodeRequest mapToBarcodeRequest(LocalDateTime dateReceived) {
        final BarcodeRequest request = new BarcodeRequest();

        request.setDateReceived(mapToFormattedDateReceived(dateReceived));

        return request;
    }

    private int mapToFormattedDateReceived(LocalDateTime dateReceived) {
        return Integer.parseInt(dateReceived.format(DateTimeFormatter.ofPattern(DATE_RECEIVED_PATTERN)));
    }
}
