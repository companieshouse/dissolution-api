package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.barcode.BarcodeRequest;
import uk.gov.companieshouse.model.dto.barcode.BarcodeResponse;

public class BarcodeFixtures {

    public static BarcodeRequest generateBarcodeRequest() {
        final BarcodeRequest request = new BarcodeRequest();

        request.setDateReceived(20200101);

        return request;
    }

    public static BarcodeResponse generateBarcodeResponse() {
        final BarcodeResponse response = new BarcodeResponse();

        response.setBarcode("BARCODE");

        return response;
    }
}
