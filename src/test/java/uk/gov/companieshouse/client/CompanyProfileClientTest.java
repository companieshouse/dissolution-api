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
import uk.gov.companieshouse.config.CompanyProfileConfig;
import uk.gov.companieshouse.model.CompanyProfile;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyProfileFixtures.generateCompanyProfile;

@ExtendWith(MockitoExtension.class)
public class CompanyProfileClientTest {

    private static final String COMPANY_NUMBER = "1234";

    private static final String API_KEY = "some-api-key";

    public static MockWebServer mockBackEnd;

    @InjectMocks
    private CompanyProfileClient client;

    @Mock
    private CompanyProfileConfig companyProfileConfig;

    @BeforeAll
    public static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());

        when(companyProfileConfig.getApiKey()).thenReturn(API_KEY);
        when(companyProfileConfig.getCompanyProfileHost()).thenReturn(baseUrl);
    }

    @Test
    public void getCompanyProfile_callsCompanyProfile_returnsCompanyProfile() throws Exception {
        final CompanyProfile company = generateCompanyProfile();

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(asJsonString(company))
        );

        final CompanyProfile result = client.getCompanyProfile(COMPANY_NUMBER);

        assertEquals(asJsonString(company), asJsonString(result));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/company/1234", recordedRequest.getPath());
    }

    @Test
    public void getCompanyProfile_callsCompanyProfile_providesTheCorrectHeaders() throws Exception {
        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.OK.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(asJsonString(generateCompanyProfile()))
        );

        client.getCompanyProfile(COMPANY_NUMBER);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals(API_KEY, recordedRequest.getHeader("Authorization"));
        assertEquals("application/json", recordedRequest.getHeader("Accept"));
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));
    }

    @Test
    public void getCompanyProfile_returnsNull_ifNoCompanyExists() throws Exception {
        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.NOT_FOUND.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody("{}")
        );

        final CompanyProfile result = client.getCompanyProfile(COMPANY_NUMBER);

        assertNull(result);
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
