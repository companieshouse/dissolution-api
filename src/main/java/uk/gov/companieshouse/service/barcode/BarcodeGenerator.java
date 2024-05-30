package uk.gov.companieshouse.service.barcode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.BarcodeGeneratorClient;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.barcode.BarcodeMapper;
import uk.gov.companieshouse.model.dto.barcode.BarcodeRequest;
import uk.gov.companieshouse.model.dto.barcode.BarcodeResponse;

import static uk.gov.companieshouse.util.DateTimeGenerator.generateCurrentDateTime;

@Service
public class BarcodeGenerator {

    private final BarcodeMapper mapper;
    private final BarcodeGeneratorClient client;

    @Autowired
    public BarcodeGenerator(BarcodeMapper mapper, BarcodeGeneratorClient client) {
        this.mapper = mapper;
        this.client = client;
    }

    public String generateBarcode() {
        final BarcodeRequest request = mapper.mapToBarcodeRequest(generateCurrentDateTime());

        final BarcodeResponse response = client.generateBarcode(request);

        return response.getBarcode();
    }
}
