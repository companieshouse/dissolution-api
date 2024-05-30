package uk.gov.companieshouse.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.config.BarcodeGeneratorConfig;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.barcode.BarcodeRequest;
import uk.gov.companieshouse.model.dto.barcode.BarcodeResponse;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.BarcodeFixtures.generateBarcodeRequest;
import static uk.gov.companieshouse.fixtures.BarcodeFixtures.generateBarcodeResponse;

@ExtendWith(MockitoExtension.class)
public class BarcodeGeneratorClientTest {

    private static final String API_KEY = "some-api-key";

    public static MockWebServer mockBackEnd;

    @InjectMocks
    private BarcodeGeneratorClient client;

    @Mock
    private BarcodeGeneratorConfig config;

    @BeforeAll
    public static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());

        when(config.getApiKey()).thenReturn(API_KEY);
        when(config.getBarcodeGeneatorHost()).thenReturn(baseUrl);
    }

    @Test
    void generateBarcode_callsBarcodeGenerator_returnsResponse() throws Exception {
        final BarcodeRequest request = generateBarcodeRequest();
        final BarcodeResponse response = generateBarcodeResponse();

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(asJsonString(response))
        );

        final BarcodeResponse result = client.generateBarcode(request);

        assertEquals(asJsonString(response), asJsonString(result));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/", recordedRequest.getPath());
        assertEquals(asJsonString(request), recordedRequest.getBody().readUtf8());
    }

    @Test
    void generateBarcode_callsBarcodeGenerator_providesTheCorrectHeaders() throws Exception {
        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(asJsonString(generateBarcodeResponse()))
        );

        client.generateBarcode(generateBarcodeRequest());

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals(API_KEY, recordedRequest.getHeader("Authorization"));
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}