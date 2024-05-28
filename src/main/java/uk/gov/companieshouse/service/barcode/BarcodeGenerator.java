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

    private final Logger logger;

    @Autowired
    public BarcodeGenerator(BarcodeMapper mapper, BarcodeGeneratorClient client, Logger logger) {
        this.mapper = mapper;
        this.client = client;
        this.logger = logger;
    }

    public String generateBarcode() {
        final BarcodeRequest request = mapper.mapToBarcodeRequest(generateCurrentDateTime());

        final BarcodeResponse response = client.generateBarcode(request);

        logger.info("Barcode Generator client response: " + response);

        return response.getBarcode();
    }
}
