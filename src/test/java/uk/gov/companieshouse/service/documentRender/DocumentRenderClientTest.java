package uk.gov.companieshouse.service.documentRender;

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
import uk.gov.companieshouse.config.DocumentRenderConfig;
import uk.gov.companieshouse.model.dto.documentRender.DissolutionCertificateData;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionCertificateData;

@ExtendWith(MockitoExtension.class)
public class DocumentRenderClientTest {

    private static final String API_KEY = "some-api-key";

    private static final String LOCATION = "s3://bucket/env";
    private static final String PDF_LOCATION = "s3://bucket/env/file.pdf";
    private static final String TEMPLATE_NAME = "ds01.html";
    private static final DissolutionCertificateData CERTIFICATE_DATA = generateDissolutionCertificateData();

    public static MockWebServer mockBackEnd;

    @InjectMocks
    private DocumentRenderClient client;

    @Mock
    private DocumentRenderConfig documentRenderConfig;

    @BeforeAll
    public static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());

        when(documentRenderConfig.getDocumentRenderHost()).thenReturn(baseUrl);
        when(documentRenderConfig.getApiKey()).thenReturn(API_KEY);
    }

    @Test
    void generateAndStoreDocument_generatesADocument_andReturnsTheStoredLocation() throws Exception {
        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.CREATED.value())
                        .addHeader("Location", PDF_LOCATION)
        );

        final String result = client.generateAndStoreDocument(CERTIFICATE_DATA, TEMPLATE_NAME, LOCATION);

        assertEquals(PDF_LOCATION, result);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/document-render/store", recordedRequest.getPath());
        assertEquals(new ObjectMapper().writeValueAsString(CERTIFICATE_DATA), recordedRequest.getBody().readUtf8());
    }

    @Test
    void generateAndStoreDocument_providesTheCorrectHeaders() throws Exception {


        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.CREATED.value())
                        .addHeader("Location", PDF_LOCATION)
        );

        final String result = client.generateAndStoreDocument(CERTIFICATE_DATA, TEMPLATE_NAME, LOCATION);

        assertEquals(PDF_LOCATION, result);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals(API_KEY, recordedRequest.getHeader("Authorization"));
        assertEquals("dissolution", recordedRequest.getHeader("assetID"));
        assertEquals(TEMPLATE_NAME, recordedRequest.getHeader("templateName"));
        assertEquals("application/pdf", recordedRequest.getHeader("Accept"));
        assertEquals("text/html", recordedRequest.getHeader("Content-Type"));
        assertEquals(LOCATION, recordedRequest.getHeader("Location"));
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

}
