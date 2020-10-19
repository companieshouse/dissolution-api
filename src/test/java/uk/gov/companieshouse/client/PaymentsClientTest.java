package uk.gov.companieshouse.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.config.PaymentsConfig;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generateRefundRequest;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generateRefundResponse;

@ExtendWith(MockitoExtension.class)
public class PaymentsClientTest {

    private static final String PAYMENT_ID = "PAY123";
    private static final String INTERNAL_API_KEY = "some-internal-api-key";

    public static MockWebServer mockBackEnd;

    @InjectMocks
    private PaymentsClient client;

    @Mock
    private PaymentsConfig config;

    @BeforeAll
    public static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());

        when(config.getApiKeyInternal()).thenReturn(INTERNAL_API_KEY);
        when(config.getPaymentsHost()).thenReturn(baseUrl);
    }


    @Test
    public void refundPayment_callsCreateRefund_returnsRefundResponse() throws Exception {
        final RefundRequest request = generateRefundRequest();
        final RefundResponse response = generateRefundResponse();

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.CREATED.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(asJsonString(response))
        );

        final Optional<RefundResponse> result = client.refundPayment(request, PAYMENT_ID);

        assertEquals(asJsonString(response), asJsonString(result.get()));

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/payments/PAY123/refunds", recordedRequest.getPath());
        assertEquals(asJsonString(request), recordedRequest.getBody().readUtf8());
    }

    @Test
    public void refundPayment_callsCreateRefund_providesTheCorrectHeaders() throws Exception {
        final RefundResponse response = generateRefundResponse();

        mockBackEnd.enqueue(
                new MockResponse()
                        .setResponseCode(HttpStatus.CREATED.value())
                        .setHeader("Content-Type", "application/json")
                        .setBody(asJsonString(response))
        );

        client.refundPayment(generateRefundRequest(), PAYMENT_ID);

        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals(INTERNAL_API_KEY, recordedRequest.getHeader("Authorization"));
        assertEquals("application/json", recordedRequest.getHeader("Accept"));
        assertEquals("application/json", recordedRequest.getHeader("Content-Type"));
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
