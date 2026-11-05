package uk.gov.companieshouse.integration;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionData;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.model.enums.SubmissionStatus;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DirectorApprovalTestDataBuilder.aDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;

class SubmitIT extends AbstractIntegrationTest {

    private static final String SUBMIT_PATH = "/dissolution-request/submit";
    private static final String HEALTHCHECK_PATH = "/healthcheck/status";
    private static final String POST_FORM_PATH = "/efilingEnablement/postForm";
    private static final String DISSOLUTION_ID = "dissolution-1";

    @Test
    void submitWithoutEricIdentityReturns401() {
        HttpHeaders headers = ericApiKeyHeaders();
        headers.remove(EricConstants.ERIC_IDENTITY);

        ResponseEntity<String> response = postSubmit(headers);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void submitWithIncorrectIdentityTypeReturns403() {
        HttpHeaders headers = ericApiKeyHeaders();
        headers.set(EricConstants.ERIC_IDENTITY_TYPE, "some-incorrect-identity-type");

        ResponseEntity<String> response = postSubmit(headers);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void submitReturns503WhenChipsIsUnavailableAndLeavesDissolutionPending() throws Exception {
        stubChips(404, 200);
        mongoTemplate.insert(pendingDissolution(null, 0));

        ResponseEntity<String> response = postSubmit(ericApiKeyHeaders());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(findRequests()).isEmpty();
        assertThat(storedSubmission().getStatus()).isEqualTo(SubmissionStatus.PENDING);
    }

    @Test
    void submitSendsPendingDissolutionToChipsAndMarksItSent() throws Exception {
        stubChips(200, 200);
        stubCertificate();
        mongoTemplate.insert(pendingDissolution(null, 0));

        ResponseEntity<String> response = postSubmit(ericApiKeyHeaders());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<RecordedRequest> postForms = findRequests();
        assertThat(postForms).hasSize(1);
        assertThat(postForms.getFirst().getBody().readUtf8()).contains("BARCODE123");

        DissolutionSubmission submission = storedSubmission();
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.SENT);
        assertThat(submission.getDateTime()).isNotNull();
        assertThat(submission.getRetryCounter()).isZero();
    }

    @Test
    void failedChipsSubmissionAtRetryLimitMarksDissolutionFailedAndSendsEmail() throws Exception {
        stubChips(200, 500);
        stubCertificate();
        mongoTemplate.insert(pendingDissolution(LocalDateTime.now().minusHours(1), 1));

        ResponseEntity<String> response = postSubmit(ericApiKeyHeaders());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(findRequests()).hasSize(1);

        DissolutionSubmission submission = storedSubmission();
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.FAILED);
        assertThat(submission.getRetryCounter()).isEqualTo(2);
        verify(emailClient).sendEmail(any());
    }

    @Test
    void failedChipsSubmissionBelowRetryLimitKeepsDissolutionPendingWithoutEmail() {
        stubChips(200, 500);
        stubCertificate();
        mongoTemplate.insert(pendingDissolution(LocalDateTime.now().minusHours(1), 0));

        ResponseEntity<String> response = postSubmit(ericApiKeyHeaders());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        DissolutionSubmission submission = storedSubmission();
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.PENDING);
        assertThat(submission.getRetryCounter()).isEqualTo(1);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void recentlyAttemptedDissolutionIsNotResubmittedUntilRetryDelayElapses() throws Exception {
        LocalDateTime attemptedAt = LocalDateTime.now().minusMinutes(5);
        stubChips(200, 200);
        mongoTemplate.insert(pendingDissolution(attemptedAt, 1));

        ResponseEntity<String> response = postSubmit(ericApiKeyHeaders());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(findRequests()).isEmpty();

        DissolutionSubmission submission = storedSubmission();
        assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.PENDING);
        assertThat(submission.getRetryCounter()).isEqualTo(1);
    }

    private ResponseEntity<String> postSubmit(HttpHeaders headers) {
        return RestClient.create()
                .post()
                .uri(baseUrl() + SUBMIT_PATH)
                .headers(h -> h.addAll(headers))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                })
                .toEntity(String.class);
    }

    private void stubChips(int healthcheckStatus, int postFormStatus) {
        mockWebServer().setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                assert request.getPath() != null;
                return switch (request.getPath()) {
                    case HEALTHCHECK_PATH -> new MockResponse().setResponseCode(healthcheckStatus);
                    case POST_FORM_PATH -> new MockResponse().setResponseCode(postFormStatus);
                    default -> new MockResponse().setResponseCode(404);
                };
            }
        });
    }

    private void stubCertificate() {
        byte[] pdf = "%PDF-1.4 test".getBytes(StandardCharsets.UTF_8);
        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream(pdf))));
    }

    private List<RecordedRequest> findRequests() throws InterruptedException {
        List<RecordedRequest> matches = new ArrayList<>();
        RecordedRequest request;
        while ((request = mockWebServer().takeRequest(0, TimeUnit.MILLISECONDS)) != null) {
            if (SubmitIT.POST_FORM_PATH.equals(request.getPath())) {
                matches.add(request);
            }
        }
        return matches;
    }

    private DissolutionSubmission storedSubmission() {
        return Objects.requireNonNull(mongoTemplate.findById(DISSOLUTION_ID, Dissolution.class)).getSubmission();
    }

    private Dissolution pendingDissolution(LocalDateTime submissionDateTime, int retryCounter) {
        DissolutionSubmission submission = new DissolutionSubmission();
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setRetryCounter(retryCounter);
        submission.setDateTime(submissionDateTime);

        PaymentInformation paymentInformation = new PaymentInformation();
        paymentInformation.setMethod(PaymentMethod.CREDIT_CARD);
        paymentInformation.setReference("PAYMENT-REF");
        paymentInformation.setDateTime(LocalDateTime.now().minusDays(1));

        DissolutionData data = DissolutionFixtures.generateDissolutionData();
        data.getApplication().setBarcode("BARCODE123");
        data.setDirectors(List.of(aDissolutionDirector()
                .withDirectorApproval(aDirectorApproval())
                .build()));

        Dissolution dissolution = aDissolution()
                .withId(DISSOLUTION_ID)
                .withData(data)
                .withSubmission(submission)
                .withPaymentInformation(paymentInformation)
                .build();
        dissolution.setCertificate(DissolutionFixtures.generateDissolutionCertificate());
        return dissolution;
    }
}
