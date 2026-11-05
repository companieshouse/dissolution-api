package uk.gov.companieshouse.integration;

import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.client.EmailClient;
import uk.gov.companieshouse.config.AbstractMongoConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.gov.companieshouse.model.Constants.HEADER_ERIC_REQUEST_ID;

/**
 * Base class for integration tests. Runs the full application on a random port against a real
 * MongoDB Testcontainer. All outbound HTTP host properties point at a single shared
 * {@link MockWebServer}; only the AWS S3 client and the email sender are mocked.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class AbstractIntegrationTest extends AbstractMongoConfig {

    protected static final String ERIC_REQUEST_ID = "XaBcDeF12345";
    protected static final String ERIC_IDENTITY_VALUE = "1234";
    protected static final String ERIC_EMAIL = "user@mail.com";
    protected static final String ERIC_ACCESS_TOKEN = "{\"token_type\":\"Bearer\",\"access_token\":\"test-token\",\"expires_in\":3600}";
    protected static final String ERIC_API_KEY_IDENTITY = "identity";

    // Fixed port: the API SDK reads its base URLs from environment variables (set in pom.xml), which cannot be changed at runtime.
    private static final int MOCK_SERVER_PORT = 8089;

    protected static final MockWebServer MOCK_WEB_SERVER = startMockWebServer();

    @MockitoBean
    protected S3Client s3Client;

    @MockitoBean
    protected EmailClient emailClient;

    @LocalServerPort
    protected int port;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @DynamicPropertySource
    static void overrideHostProperties(DynamicPropertyRegistry registry) {
        final String mockBaseUrl = String.format("http://localhost:%s", MOCK_WEB_SERVER.getPort());
        registry.add("api.url", () -> mockBaseUrl);
        registry.add("barcodeGenerator.host", () -> mockBaseUrl);
        registry.add("cdn.host", () -> mockBaseUrl);
        registry.add("chips.host", () -> mockBaseUrl);
        registry.add("companyProfile.host", () -> mockBaseUrl);
        registry.add("documentRender.host", () -> mockBaseUrl);
        registry.add("kafka.api.url", () -> mockBaseUrl);
        registry.add("payments.url", () -> mockBaseUrl);
    }

    @BeforeEach
    void resetState() {
        resetMongo();
        resetMockWebServer();
    }

    protected String baseUrl() {
        return String.format("http://localhost:%s", port);
    }

    /**
     * Built per call because {@link #port} is only injected after the test instance is constructed.
     */
    protected RestClient restClient() {
        return RestClient.create(baseUrl());
    }

    /**
     * Reads the response body and status without throwing on 4xx/5xx, so tests can assert on error codes.
     */
    protected static ResponseEntity<String> toEntity(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response)
            throws IOException {
        return ResponseEntity.status(response.getStatusCode()).body(response.bodyTo(String.class));
    }

    protected MockWebServer mockWebServer() {
        return MOCK_WEB_SERVER;
    }

    /**
     * Headers for a signed-in user acting on a company, as provided by the ERIC gateway.
     */
    protected HttpHeaders ericUserHeaders(final String companyNumber) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, ERIC_IDENTITY_VALUE);
        headers.add("ERIC-Authorised-User", ERIC_EMAIL);
        headers.add(HEADER_ERIC_REQUEST_ID, ERIC_REQUEST_ID);
        headers.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER, companyNumber,
                Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE
        ));
        headers.add("ERIC-Access-Token", ERIC_ACCESS_TOKEN);
        return headers;
    }

    /**
     * Headers for an internal API-key caller, as provided by the ERIC gateway.
     */
    protected HttpHeaders ericApiKeyHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, ERIC_API_KEY_IDENTITY);
        headers.add(EricConstants.ERIC_IDENTITY_TYPE, SecurityConstants.API_KEY_IDENTITY_TYPE);
        headers.add(EricConstants.ERIC_AUTHORISED_KEY_ROLES, SecurityConstants.INTERNAL_USER_ROLE);
        headers.add("ERIC-Access-Token", ERIC_ACCESS_TOKEN);
        headers.add(HEADER_ERIC_REQUEST_ID, ERIC_REQUEST_ID);
        return headers;
    }

    private void resetMongo() {
        mongoTemplate.getCollectionNames().stream()
                .filter(name -> !name.startsWith("system."))
                .forEach(mongoTemplate::dropCollection);
    }

    private void resetMockWebServer() {
        // Replacing the dispatcher discards any queued responses; then drain recorded requests.
        MOCK_WEB_SERVER.setDispatcher(new QueueDispatcher());
        try {
            // NOSONAR java:S108 - each take removes one recorded request; the loop ends when none remain.
            while (MOCK_WEB_SERVER.takeRequest(0, TimeUnit.MILLISECONDS) != null) {
                // discard
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while resetting MockWebServer", e);
        }
    }

    private static MockWebServer startMockWebServer() {
        final MockWebServer server = new MockWebServer();
        try {
            server.start(MOCK_SERVER_PORT);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to start MockWebServer", e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.shutdown();
            } catch (IOException e) {
                // Best-effort cleanup during JVM exit.
            }
        }));
        return server;
    }
}
