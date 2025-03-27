package uk.gov.companieshouse.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.client.EmailClient;
import uk.gov.companieshouse.fixtures.EmailFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private EmailClient emailClient;

    @Mock
    private Logger logger;

    @Test
    void givenValidPayload_whenSendEmail_thenReturnOk() {
        final EmailDocument<?> emailDocument = EmailFixtures.generateEmailDocument("some-email-data");

        ApiResponse<Void> apiResponse = new ApiResponse<>(200, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        emailService.sendMessage(emailDocument);

        verify(emailClient, times(1)).sendEmail(emailDocument);
        verify(logger, times(1)).info("Sending email document to CHS Kafka API...");

        assertThat(apiResponse.getStatusCode(), is(200));
    }

    @Test
    void givenInvalidPayload_whenSendEmail_thenReturnBadRequest() {
        final EmailDocument<?> emailDocument = EmailFixtures.generateEmailDocument("some-email-data");

        ApiResponse<Void> apiResponse = new ApiResponse<>(400, Map.of());
        when(emailClient.sendEmail(emailDocument)).thenReturn(apiResponse);

        emailService.sendMessage(emailDocument);

        verify(emailClient, times(1)).sendEmail(emailDocument);
        verify(logger, times(1)).info("Sending email document to CHS Kafka API...");

        assertThat(apiResponse.getStatusCode(), is(400));
    }
}
