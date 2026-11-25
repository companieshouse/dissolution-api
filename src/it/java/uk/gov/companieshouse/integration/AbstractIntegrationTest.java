package uk.gov.companieshouse.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.google.api.client.auth.oauth2.TokenResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.mongodb.MongoDBContainer;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.http.OAuthHttpClient;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.config.AbstractMongoConfig;

/**
 * Base class for integration tests. Runs the full application on a random port against a shared
 * MongoDB Testcontainer, with every outbound HTTP dependency pointed at WireMock on a random port.
 *
 * <p>Keep all {@code @MockitoBean}s here: declaring one in a subclass changes the context cache key,
 * starting another Spring context (and WireMock server) for that class.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureRestTestClient
// Setting baseUrlProperties replaces the default, so wiremock.server.baseUrl must be listed explicitly.
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = {
        "wiremock.server.baseUrl",
        "api.url",
        "barcodeGenerator.host",
        "cdn.host",
        "chips.host",
        "companyProfile.host",
        "documentRender.host",
        "kafka.api.url",
        "payments.url"
}))
public abstract class AbstractIntegrationTest {

    private static final String TEST_API_KEY = "test-api-key";

    @ServiceConnection
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(AbstractMongoConfig.MONGO_IMAGE);

    static {
        mongoDBContainer.start();
    }

    @Autowired
    protected RestTestClient client;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected JsonMapper jsonMapper;

    // The real ApiClientService builds SDK clients from API_URL etc. environment variables, which
    // can't be changed at runtime. Mocking it lets the SDK target WireMock's random port instead,
    // while still authenticating with whatever pass-through token the application hands it.
    @MockitoBean
    protected ApiClientService apiClientService;

    @Value("${wiremock.server.baseUrl}")
    private String wireMockBaseUrl;

    @BeforeEach
    void resetState() throws IOException {
        mongoTemplate.getDb().drop();
        stubSdkClients();
    }

    private void stubSdkClients() throws IOException {
        when(apiClientService.getApiClient()).thenAnswer(inv -> configureWireMock(new ApiClient(new ApiKeyHttpClient(TEST_API_KEY))));
        when(apiClientService.getInternalApiClient()).thenAnswer(inv -> internalApiClient(new ApiKeyHttpClient(TEST_API_KEY)));
        // any() rather than anyString(): the pass-through header is null when no ERIC-Access-Token is sent.
        when(apiClientService.getApiClient(any())).thenAnswer(inv -> configureWireMock(new ApiClient(httpClientFor(inv.getArgument(0)))));
        when(apiClientService.getInternalApiClient(any())).thenAnswer(inv -> internalApiClient(httpClientFor(inv.getArgument(0))));
    }

    private InternalApiClient internalApiClient(HttpClient httpClient) {
        final InternalApiClient internalApiClient = configureWireMock(new InternalApiClient(httpClient));
        internalApiClient.setInternalBasePath(wireMockBaseUrl);
        return internalApiClient;
    }

    /**
     * Mirrors {@code ApiSdkManager.getSDK(passthroughHeader)}: a Bearer pass-through token becomes an
     * {@link OAuthHttpClient}, so tests can verify the caller's token reaches downstream APIs.
     */
    private HttpClient httpClientFor(String passthroughHeader) throws IOException {
        if (passthroughHeader == null || passthroughHeader.isEmpty()) {
            return new ApiKeyHttpClient(TEST_API_KEY);
        }
        final String accessToken = jsonMapper.readTree(passthroughHeader).get("access_token").asString();
        return new OAuthHttpClient(new TokenResponse().setAccessToken(accessToken));
    }

    private <T extends ApiClient> T configureWireMock(T apiClient) {
        apiClient.setBasePath(wireMockBaseUrl);
        apiClient.setBasePaymentsPath(wireMockBaseUrl);
        apiClient.setBaseDocumentAPIPath(wireMockBaseUrl);
        apiClient.setBaseOracleQueryApiPath(wireMockBaseUrl);
        return apiClient;
    }

    protected String toJson(Object body) {
        return jsonMapper.writeValueAsString(body);
    }
}
