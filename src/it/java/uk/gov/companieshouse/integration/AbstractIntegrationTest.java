package uk.gov.companieshouse.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.gov.companieshouse.model.Constants.HEADER_ERIC_REQUEST_ID;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
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
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.Permission;

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

    protected static final String ERIC_REQUEST_ID = "XaBcDeF12345";
    protected static final String ERIC_IDENTITY_VALUE = "user123";
    protected static final String ERIC_EMAIL = "user@mail.com";
    protected static final String ERIC_ACCESS_TOKEN = "{\"token_type\":\"Bearer\",\"access_token\":\"test-token\",\"expires_in\":3600}";

    // Started once for every IT class rather than via @Container, which would stop it after each class
    // while Spring's cached context still points at it.
    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.2.5-noble");

    static {
        MONGO.start();
    }

    @Autowired
    protected RestTestClient client;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected JsonMapper jsonMapper;

    // The real ApiClientService builds SDK clients from API_URL etc. environment variables, which
    // can't be changed at runtime. Mocking it lets the SDK target WireMock's random port instead.
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
        final ApiClient apiClient = pointAtWireMock(new ApiClient(new ApiKeyHttpClient("test-api-key")));
        final InternalApiClient internalApiClient = pointAtWireMock(new InternalApiClient(new ApiKeyHttpClient("test-api-key")));
        internalApiClient.setInternalBasePath(wireMockBaseUrl);

        // any() rather than anyString(): the pass-through header is null when no ERIC-Access-Token is sent.
        when(apiClientService.getApiClient()).thenReturn(apiClient);
        when(apiClientService.getApiClient(any())).thenReturn(apiClient);
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(apiClientService.getInternalApiClient(any())).thenReturn(internalApiClient);
    }

    private <T extends ApiClient> T pointAtWireMock(T apiClient) {
        apiClient.setBasePath(wireMockBaseUrl);
        apiClient.setBasePaymentsPath(wireMockBaseUrl);
        apiClient.setBaseDocumentAPIPath(wireMockBaseUrl);
        apiClient.setBaseOracleQueryApiPath(wireMockBaseUrl);
        return apiClient;
    }

    protected String toJson(Object body) {
        return jsonMapper.writeValueAsString(body);
    }

    /**
     * Headers for a signed-in user authorised to dissolve the given company, as provided by the ERIC gateway.
     */
    protected HttpHeaders ericUserHeaders(final String companyNumber) {
        return ericUserHeaders(ERIC_IDENTITY_VALUE, companyNumber);
    }

    protected HttpHeaders ericUserHeaders(final String userId, final String companyNumber) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, userId);
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
}
