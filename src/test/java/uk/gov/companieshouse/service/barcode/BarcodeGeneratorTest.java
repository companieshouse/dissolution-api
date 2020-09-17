package uk.gov.companieshouse.service.barcode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.BarcodeGeneratorClient;
import uk.gov.companieshouse.mapper.barcode.BarcodeMapper;
import uk.gov.companieshouse.model.dto.barcode.BarcodeRequest;
import uk.gov.companieshouse.model.dto.barcode.BarcodeResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.BarcodeFixtures.generateBarcodeRequest;
import static uk.gov.companieshouse.fixtures.BarcodeFixtures.generateBarcodeResponse;

@ExtendWith(MockitoExtension.class)
public class BarcodeGeneratorTest {

    private static final String BARCODE = "B4RC0D3";

    @InjectMocks
    private BarcodeGenerator generator;

    @Mock
    private BarcodeMapper mapper;

    @Mock
    private BarcodeGeneratorClient client;

    @Test
    public void generateBarcode_createsABarcodeRequest_retrivesBarcode() {
        final BarcodeRequest request = generateBarcodeRequest();
        final BarcodeResponse response = generateBarcodeResponse();
        response.setBarcode(BARCODE);

        when(mapper.mapToBarcodeRequest(any())).thenReturn(request);
        when(client.generateBarcode(request)).thenReturn(response);

        final String result = generator.generateBarcode();

        verify(client).generateBarcode(request);

        assertEquals(BARCODE, result);
    }
}
