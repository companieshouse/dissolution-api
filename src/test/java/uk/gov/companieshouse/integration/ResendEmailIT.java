package uk.gov.companieshouse.integration;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.exception.EmailSendException;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.MessageType;
import uk.gov.companieshouse.model.dto.email.SignatoryToSignEmailData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ResendEmailIT extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String SIGNATORY_EMAIL = "signatory@mail.com";

    @Test
    void resendEmailSendsSignatoryEmailForActiveDissolution() {
        aDissolution(true);

        final HttpStatusCode status = postResendEmail(ericApiKeyHeaders());

        assertThat(status).isEqualTo(HttpStatus.OK);

        final ArgumentCaptor<EmailDocument<?>> captor = ArgumentCaptor.captor();
        verify(emailClient).sendEmail(captor.capture());
        final EmailDocument<?> sent = captor.getValue();
        assertThat(sent.getEmailAddress()).isEqualTo(SIGNATORY_EMAIL);
        assertThat(sent.getMessageType()).isEqualTo(MessageType.SIGNATORY_TO_SIGN.getValue());

        final SignatoryToSignEmailData data = (SignatoryToSignEmailData) sent.getData();
        assertThat(data.getCompanyNumber()).isEqualTo(COMPANY_NUMBER);
        assertThat(data.getDissolutionReferenceNumber()).isEqualTo("ABC123");

        final List<Dissolution> stored = mongoTemplate.findAll(Dissolution.class);
        assertThat(stored).hasSize(1);
        assertThat(stored.getFirst().getActive()).isTrue();
        assertThat(stored.getFirst().getCompany().getNumber()).isEqualTo(COMPANY_NUMBER);
    }

    @Test
    void resendEmailReturnsNotFoundWhenNoDissolutionExists() {
        final HttpStatusCode status = postResendEmail(ericApiKeyHeaders());

        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void resendEmailReturnsNotFoundWhenDissolutionIsInactive() {
        aDissolution(false);

        final HttpStatusCode status = postResendEmail(ericApiKeyHeaders());

        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void resendEmailReturnsUnauthorisedWhenNoIdentityHeaders() {
        aDissolution(true);

        final HttpStatusCode status = postResendEmail(new HttpHeaders());

        assertThat(status).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void resendEmailReturnsForbiddenForUserIdentity() {
        aDissolution(true);

        final HttpStatusCode status = postResendEmail(ericUserHeaders(COMPANY_NUMBER));

        assertThat(status).isEqualTo(HttpStatus.FORBIDDEN);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void resendEmailReturnsForbiddenForApiKeyWithoutInternalRole() {
        aDissolution(true);
        final HttpHeaders headers = ericApiKeyHeaders();
        headers.remove(EricConstants.ERIC_AUTHORISED_KEY_ROLES);

        final HttpStatusCode status = postResendEmail(headers);

        assertThat(status).isEqualTo(HttpStatus.FORBIDDEN);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void resendEmailReturnsNotFoundWhenEmailSendFails() {
        aDissolution(true);
        doThrow(new EmailSendException("email service unavailable")).when(emailClient).sendEmail(any());

        // The controller converts any failure, including email send errors, into a 404.
        final HttpStatusCode status = postResendEmail(ericApiKeyHeaders());

        assertThat(status).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void aDissolution(boolean active) {
        final Dissolution dissolution = DissolutionTestDataBuilder.aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withActive(active)
                .build();
        mongoTemplate.insert(dissolution);
    }

    private HttpStatusCode postResendEmail(HttpHeaders headers) {
        return RestClient.create()
                .post()
                .uri(baseUrl() + "/dissolution-request/{companyNumber}/resend-email/{emailAddress}", ResendEmailIT.COMPANY_NUMBER, ResendEmailIT.SIGNATORY_EMAIL)
                .headers(h -> h.putAll(headers))
                .retrieve()
                .onStatus(status -> true, (request, response) -> {
                    // Accept every status so the test can assert it.
                })
                .toBodilessEntity()
                .getStatusCode();
    }
}
