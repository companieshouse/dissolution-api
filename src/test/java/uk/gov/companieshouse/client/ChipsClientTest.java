package uk.gov.companieshouse.client;

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
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.config.ChipsConfig;
import uk.gov.companieshouse.exception.ChipsNotAvailableException;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.ChipsFixtures.generateDissolutionChipsRequest;

@ExtendWith(MockitoExtension.class)
public class ChipsClientTest {

    public static MockWebServer mockBackEnd;

    @InjectMocks
    private ChipsClient client;

    @Mock
    private ChipsConfig config;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());

        when(config.getChipsHost()).thenReturn(baseUrl);
    }

    @Test
    void isAvailable_callsChipsHealthcheck_returnsFalseIfNotFoundReturned() throws InterruptedException {
        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
        );

        final boolean result = client.isAvailable();

        assertFalse(result);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/healthcheck/status", recordedRequest.getPath());
    }

    @Test
    void isAvailable_callsChipsHealthcheck_returnsTrueIfOkReturned() throws InterruptedException {
        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
        );

        final boolean result = client.isAvailable();

        assertTrue(result);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/healthcheck/status", recordedRequest.getPath());
    }

    @Test
    void sendDissolutionToChips_sendsDissolutionRequestToChips() throws InterruptedException, JacksonException {
        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/json")
        );

        client.sendDissolutionToChips(request);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/efilingEnablement/postForm", recordedRequest.getPath());
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));
        // Compare JSON objects to avoid order issues
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(
            mapper.readTree(asJsonString(request)),
            mapper.readTree(recordedRequest.getBody().readUtf8())
        );
    }

    @Test
    void sendDissolutionToChips_throwsChipsNotAvailableException_ifNotFoundIsReturned() {
        final DissolutionChipsRequest request = generateDissolutionChipsRequest();

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setHeader("Content-Type", "application/json")
        );

        assertThrows(ChipsNotAvailableException.class, () -> client.sendDissolutionToChips(request));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    private String asJsonString(Object obj) throws JacksonException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
