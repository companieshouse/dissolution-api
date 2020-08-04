package uk.gov.companieshouse.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.api.model.officers.OfficersApi;
import uk.gov.companieshouse.config.ApiConfig;
import uk.gov.companieshouse.config.CompanyOfficersConfig;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficerList;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateOfficersApi;

@ExtendWith(MockitoExtension.class)
public class CompanyOfficersClientTest {

    private static final String COMPANY_NUMBER = "1234";

    private static final String API_KEY = "some-api-key";

    public static MockWebServer mockBackEnd;

    @InjectMocks
    private CompanyOfficersClient client;

    @Mock
    private CompanyOfficersConfig config;

    @BeforeAll
    public static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());

        when(config.getApiKey()).thenReturn(API_KEY);
        when(config.getApiUrl()).thenReturn(baseUrl);
    }

    @Test
    void getCompanyOfficers_callsCompanyOfficersApi_extractsOfficerItems() throws Exception {
        final List<CompanyOfficerApi> officers = generateCompanyOfficerList();
        final OfficersApi officersApi = generateOfficersApi();

        officersApi.setItems(officers);

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(asJsonString(officersApi))
        );

        final List<CompanyOfficerApi> result = client.getCompanyOfficers(COMPANY_NUMBER);

        assertEquals(asJsonString(officers), asJsonString(result));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/company/1234/officers", recordedRequest.getPath());
    }

    @Test
    void getCompanyOfficers_callsCompanyOfficersApi_providesTheCorrectHeaders() throws Exception {
        final List<CompanyOfficerApi> officers = generateCompanyOfficerList();
        final OfficersApi officersApi = generateOfficersApi();

        officersApi.setItems(officers);

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(asJsonString(officersApi))
        );

        client.getCompanyOfficers(COMPANY_NUMBER);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals(API_KEY, recordedRequest.getHeader("Authorization"));
        assertEquals("application/json", recordedRequest.getHeader("Accept"));
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));
    }

    @Test
    void getCompanyOfficers_returnsEmptyList_ifCompanyDoesNotExist() throws Exception {
        final List<CompanyOfficerApi> officers = generateCompanyOfficerList();
        final OfficersApi officersApi = generateOfficersApi();

        officersApi.setItems(officers);

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody("{}")
        );

        final List<CompanyOfficerApi> result = client.getCompanyOfficers(COMPANY_NUMBER);

        assertTrue(result.isEmpty());
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .writeValueAsString(obj);
    }
}
