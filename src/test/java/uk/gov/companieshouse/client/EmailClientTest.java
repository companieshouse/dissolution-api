package uk.gov.companieshouse.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.SendEmail;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateSendEmailHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateSendEmailPost;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.exception.EmailSendException;
import uk.gov.companieshouse.fixtures.EmailFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.email.*;

import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailClientTest {

    @Mock
    private Supplier<InternalApiClient> internalApiClientSupplier;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private Logger logger;

    @InjectMocks
    private EmailClient emailClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailClient, "objectMapper", new ObjectMapper());
    }

    @AfterEach
    void tearDown() {}

    @Test
    void givenValidPayload_whenSignatoryToSignEmailData_thenReturnSuccess() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        SignatoryToSignEmailData data = EmailFixtures.generateSignatoryToSignEmailData();
        EmailDocument<SignatoryToSignEmailData> document = EmailFixtures.generateEmailDocument(data);

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    void givenValidPayload_whenSupportNotificationEmailData_thenReturnSuccess() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        SupportNotificationEmailData data = EmailFixtures.generateSupportNotificationEmailData();
        EmailDocument<SupportNotificationEmailData> document = EmailFixtures.generateEmailDocument(data);

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    void givenValidPayload_whenApplicationRejectedEmailData_thenReturnSuccess() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        ApplicationRejectedEmailData data = EmailFixtures.generateApplicationRejectedEmailData();
        EmailDocument<ApplicationRejectedEmailData> document = EmailFixtures.generateEmailDocument(data);

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    void givenValidPayload_whenApplicationAcceptedEmailData_thenReturnSuccess() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        ApplicationAcceptedEmailData data = EmailFixtures.generateApplicationAcceptedEmailData();
        EmailDocument<ApplicationAcceptedEmailData> document = EmailFixtures.generateEmailDocument(data);

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    void givenValidPayload_whenSuccessfulPaymentEmailData_thenReturnSuccess() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        SuccessfulPaymentEmailData data = EmailFixtures.generateSuccessfulPaymentEmailData();
        EmailDocument<SuccessfulPaymentEmailData> document = EmailFixtures.generateEmailDocument(data);

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    void givenValidPayload_whenSuccessfulPaymentEmailDataForDirector_thenReturnSuccess() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        SuccessfulPaymentEmailData data = EmailFixtures.generateSuccessfulPaymentEmailDataForDirector();
        EmailDocument<SuccessfulPaymentEmailData> document = EmailFixtures.generateEmailDocument(data);

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    void givenValidPayload_whenPendingPaymentEmailData_thenReturnSuccess() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        PendingPaymentEmailData data = EmailFixtures.generatePendingPaymentEmailData();
        EmailDocument<PendingPaymentEmailData> document = EmailFixtures.generateEmailDocument(data);

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(200));
    }

    @Test
    void givenInvalidPayload_whenConfirmationEmailRequested_thenReturnBadRequest() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(400, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        SuccessfulPaymentEmailData data = EmailFixtures.generateSuccessfulPaymentEmailData();
        EmailDocument<SuccessfulPaymentEmailData> document = EmailFixtures.generateEmailDocument(data);

        document.setEmailAddress("");

        // Act:
        ApiResponse<Void> response = emailClient.sendEmail(document);

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(response.getStatusCode(), is(400));
    }

    @Test
    void givenValidPayload_whenConfirmationEmailClientThrowsApiException_thenReturnError() throws ApiErrorResponseException {
        // Arrange:
        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenThrow(ApiErrorResponseException.class);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

        SuccessfulPaymentEmailData data = EmailFixtures.generateSuccessfulPaymentEmailData();
        EmailDocument<SuccessfulPaymentEmailData> document = EmailFixtures.generateEmailDocument(data);

        // Act:
        EmailSendException emailSendingException = assertThrows(EmailSendException.class, () ->
                emailClient.sendEmail(document)
        );

        // Assert:
        verify(internalApiClient, times(1)).sendEmailHandler();
        verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
        verify(privateSendEmailPost, times(1)).execute();

        assertThat(emailSendingException.getMessage(), nullValue());
    }

    @Test
    void givenInvalidPayload_whenConfirmationEmailClientThrowsJsonException_thenReturnError() throws JsonProcessingException {
        // Arrange:
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        ReflectionTestUtils.setField(emailClient, "objectMapper", mockObjectMapper);

        SuccessfulPaymentEmailData data = EmailFixtures.generateSuccessfulPaymentEmailData();
        EmailDocument<SuccessfulPaymentEmailData> document = EmailFixtures.generateEmailDocument(data);

        // Act:
        EmailSendException emailSendingException = assertThrows(EmailSendException.class, () ->
                emailClient.sendEmail(document)
        );

        // Assert:
        verifyNoInteractions(internalApiClient);

        assertThat(emailSendingException.getMessage(), is("N/A"));
    }

    @Test
    void givenValidPayload_whenHttpRequestIdUnavailable_thenReturnEmptyRequestId() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        // For structured logging we need to be able to mock the request at the service-level.
        try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("x-request-id")).thenReturn("this-is-my-request-id");

            ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
            when(requestAttributes.getRequest()).thenReturn(request);

            mocked.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            when(internalApiClientSupplier.get()).thenReturn(internalApiClient);

            HttpClient httpClient = mock(HttpClient.class);
            when(internalApiClient.getHttpClient()).thenReturn(httpClient);
            when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

            SuccessfulPaymentEmailData data = EmailFixtures.generateSuccessfulPaymentEmailData();
            EmailDocument<SuccessfulPaymentEmailData> document = EmailFixtures.generateEmailDocument(data);

            // Act:
            ApiResponse<Void> response = emailClient.sendEmail(document);

            // Assert:
            verify(httpClient, times(1)).setRequestId("this-is-my-request-id");
            verify(internalApiClient, times(1)).sendEmailHandler();
            verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
            verify(privateSendEmailPost, times(1)).execute();

            mocked.verify(RequestContextHolder::getRequestAttributes, times(1));

            assertThat(response.getStatusCode(), is(200));
        }
    }

    @Test
    void givenValidPayload_whenHttpRequestIdAvailable_thenReturnEmptyRequestId() throws ApiErrorResponseException {
        // Arrange:
        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());

        PrivateSendEmailPost privateSendEmailPost = mock(PrivateSendEmailPost.class);
        when(privateSendEmailPost.execute()).thenReturn(apiResponse);

        PrivateSendEmailHandler privateSendEmailHandler = mock(PrivateSendEmailHandler.class);
        when(privateSendEmailHandler.postSendEmail(eq("/send-email"), any(SendEmail.class))).thenReturn(privateSendEmailPost);

        // For structured logging we need to be able to mock the request at the service-level.
        try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("x-request-id")).thenReturn(null);

            ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
            when(requestAttributes.getRequest()).thenReturn(request);

            mocked.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);

            when(internalApiClientSupplier.get()).thenReturn(internalApiClient);

            HttpClient httpClient = mock(HttpClient.class);
            when(internalApiClient.getHttpClient()).thenReturn(httpClient);
            when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);

            SuccessfulPaymentEmailData data = EmailFixtures.generateSuccessfulPaymentEmailData();
            EmailDocument<SuccessfulPaymentEmailData> document = EmailFixtures.generateEmailDocument(data);

            // Act:
            ApiResponse<Void> response = emailClient.sendEmail(document);

            // Assert:
            verify(httpClient, times(1)).setRequestId(argThat(s -> s.length() == 36));
            verify(internalApiClient, times(1)).sendEmailHandler();
            verify(privateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), any(SendEmail.class));
            verify(privateSendEmailPost, times(1)).execute();

            mocked.verify(RequestContextHolder::getRequestAttributes, times(1));

            assertThat(response.getStatusCode(), is(200));
        }
    }


}
