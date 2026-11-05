package uk.gov.companieshouse.integration;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.MessageType;
import uk.gov.companieshouse.model.enums.VerdictResult;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.fixtures.ChipsFixtures;
import uk.gov.companieshouse.model.dto.chips.RejectReason;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;

class ResponseIT extends AbstractIntegrationTest {

    private static final String SUBMISSION_REFERENCE = "XYZ456";
    private static final String APPLICANT_EMAIL = "applicant@example.com";
    private static final String FINANCE_EMAIL = "finance@example.com";
    private static final String PAYMENT_REFERENCE = "ABCDEFGH1234567";
    private static final String REFUND_JSON = """
            {"refund_id":"REF123","created_date_time":"2025-01-01T10:00:00","amount":1300}
            """;

    @Test
    void postResponse_acceptedVerdict_returnsOkAndStoresVerdictAndSendsAcceptedEmail() {
        mongoTemplate.insert(aDissolution()
                .withApplicationReference(SUBMISSION_REFERENCE)
                .withCreatedByEmail(APPLICANT_EMAIL)
                .build());

        ChipsResponseCreateRequest request = ChipsFixtures.generateChipsResponseCreateRequest();
        request.setStatus(VerdictResult.ACCEPTED);

        ResponseEntity<Void> response = postResponse(ericApiKeyHeaders(), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Dissolution stored = findStoredDissolution();
        assertThat(stored.getActive()).isFalse();
        assertThat(stored.getVerdict().getResult()).isEqualTo(VerdictResult.ACCEPTED);
        assertThat(stored.getVerdict().getDateTime()).isNotNull();

        ArgumentCaptor<EmailDocument<?>> emailCaptor = ArgumentCaptor.captor();
        verify(emailClient).sendEmail(emailCaptor.capture());
        assertThat(emailCaptor.getValue().getEmailAddress()).isEqualTo(APPLICANT_EMAIL);
        assertThat(emailCaptor.getValue().getMessageType()).isEqualTo(MessageType.APPLICATION_ACCEPTED.getValue());
    }

    @Test
    void postResponse_rejectedCreditCardVerdict_refundsPaymentStoresRefundAndSendsRejectedEmail() throws Exception {
        mongoTemplate.insert(aDissolution()
                .withApplicationReference(SUBMISSION_REFERENCE)
                .withCreatedByEmail(APPLICANT_EMAIL)
                .withPaymentInformation(creditCardPayment())
                .build());
        mockPaymentsRefund(200, REFUND_JSON);

        ResponseEntity<Void> response = postResponse(ericApiKeyHeaders(), rejectedRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        RecordedRequest refundRequest = mockWebServer().takeRequest(5, TimeUnit.SECONDS);
        assertThat(refundRequest).isNotNull();
        assertThat(refundRequest.getPath()).isEqualTo("/payments/" + PAYMENT_REFERENCE + "/refunds");
        assertThat(refundRequest.getBody().readUtf8()).contains("\"amount\":1300");

        Dissolution stored = findStoredDissolution();
        assertThat(stored.getActive()).isFalse();
        assertThat(stored.getVerdict().getResult()).isEqualTo(VerdictResult.REJECTED);
        assertThat(stored.getVerdict().getRejectReasons()).hasSize(1);
        assertThat(stored.getVerdict().getRejectReasons().getFirst().getTextEnglish()).isEqualTo("some reject reason");
        assertThat(stored.getPaymentInformation().getRefund().getRefundId()).isEqualTo("REF123");
        assertThat(stored.getPaymentInformation().getRefund().getAmount()).isEqualTo(1300);

        ArgumentCaptor<EmailDocument<?>> emailCaptor = ArgumentCaptor.captor();
        verify(emailClient, atLeastOnce()).sendEmail(emailCaptor.capture());
        assertThat(emailCaptor.getAllValues())
                .extracting(EmailDocument::getEmailAddress)
                .containsExactly(APPLICANT_EMAIL);
        assertThat(emailCaptor.getValue().getMessageType()).isEqualTo(MessageType.APPLICATION_REJECTED.getValue());
    }

    @Test
    void postResponse_rejectedCreditCardVerdictWhenRefundFails_sendsRejectionEmailToFinance() {
        mongoTemplate.insert(aDissolution()
                .withApplicationReference(SUBMISSION_REFERENCE)
                .withCreatedByEmail(APPLICANT_EMAIL)
                .withPaymentInformation(creditCardPayment())
                .build());
        mockPaymentsRefund(500, "");

        ResponseEntity<Void> response = postResponse(ericApiKeyHeaders(), rejectedRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Dissolution stored = findStoredDissolution();
        assertThat(stored.getActive()).isFalse();
        assertThat(stored.getVerdict().getResult()).isEqualTo(VerdictResult.REJECTED);
        assertThat(stored.getPaymentInformation().getRefund()).isNull();

        ArgumentCaptor<EmailDocument<?>> emailCaptor = ArgumentCaptor.captor();
        verify(emailClient, atLeastOnce()).sendEmail(emailCaptor.capture());
        assertThat(emailCaptor.getAllValues())
                .extracting(EmailDocument::getEmailAddress)
                .contains(FINANCE_EMAIL);
    }

    @Test
    void postResponse_unknownSubmissionReference_returnsNotFoundAndStoresNothing() {
        mongoTemplate.insert(aDissolution()
                .withApplicationReference("OTHER999")
                .withCreatedByEmail(APPLICANT_EMAIL)
                .build());

        ResponseEntity<Void> response = postResponse(ericApiKeyHeaders(), rejectedRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Dissolution unchanged = mongoTemplate.findOne(
                Query.query(Criteria.where("data.application.reference").is("OTHER999")), Dissolution.class);
        assertThat(unchanged).isNotNull();
        assertThat(unchanged.getActive()).isTrue();
        assertThat(unchanged.getVerdict().getResult()).isNull();
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void postResponse_withoutEricIdentity_returnsUnauthorised() {
        HttpHeaders headers = ericApiKeyHeaders();
        headers.remove(EricConstants.ERIC_IDENTITY);

        ResponseEntity<Void> response = postResponse(headers, ChipsFixtures.generateChipsResponseCreateRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void postResponse_withWrongIdentityType_returnsForbidden() {
        HttpHeaders headers = ericApiKeyHeaders();
        headers.set(EricConstants.ERIC_IDENTITY_TYPE, "oauth2");

        ResponseEntity<Void> response = postResponse(headers, ChipsFixtures.generateChipsResponseCreateRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private ResponseEntity<Void> postResponse(HttpHeaders headers, ChipsResponseCreateRequest body) {
        return RestClient.create()
                .post()
                .uri(baseUrl() + "/dissolution-request/response")
                .headers(h -> h.addAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                })
                .toBodilessEntity();
    }

    private void mockPaymentsRefund(int responseCode, String body) {
        mockWebServer().setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                assert request.getPath() != null;
                if (request.getPath().startsWith("/payments/")) {
                    return new MockResponse()
                            .setResponseCode(responseCode)
                            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .setBody(body);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
    }

    private ChipsResponseCreateRequest rejectedRequest() {
        ChipsResponseCreateRequest request = ChipsFixtures.generateChipsResponseCreateRequest();
        request.setStatus(VerdictResult.REJECTED);
        request.setRejectReasons(new RejectReason[]{ChipsFixtures.generateChipsRejectReason()});
        return request;
    }

    private PaymentInformation creditCardPayment() {
        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setMethod(PaymentMethod.CREDIT_CARD);
        paymentInformation.setReference(PAYMENT_REFERENCE);
        paymentInformation.setDateTime(LocalDateTime.now());
        return paymentInformation;
    }

    private Dissolution findStoredDissolution() {
        Dissolution stored = mongoTemplate.findOne(
                Query.query(Criteria.where("data.application.reference").is(SUBMISSION_REFERENCE)), Dissolution.class);
        assertThat(stored).isNotNull();
        return stored;
    }
}
