package uk.gov.companieshouse.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class KafkaClientTest {

    private static final String SCHEMA_URI = "/some-uri";

    public static MockWebServer mockBackEnd;

    private final KafkaClient client = new KafkaClient();

    @BeforeAll
    public static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @Test
    public void getSchema_retrievesSchema() throws Exception {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());

        final String response = "{\"schema\": {}}";

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/vnd.schemaregistry.v1+json")
                        .setBody(response)
        );

        final String result = client.getSchema(baseUrl, SCHEMA_URI);

        assertEquals(response, result);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(SCHEMA_URI, recordedRequest.getPath());
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }
}
