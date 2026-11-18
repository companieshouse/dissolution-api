package uk.gov.companieshouse.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.PaymentMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class PaymentIT extends AbstractIntegrationTest {

    private static final String PAYMENT_PATH = "/dissolution-request/{application-reference}/payment";
    private static final String APPLICATION_REFERENCE = "PAYREF000001";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String PAYMENT_REFERENCE = "ABCDEFGH1234567";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void getPayment_returnsOk_withPaymentDetails() {
        insertDissolution(ApplicationStatus.PENDING_PAYMENT);

        final ResponseEntity<String> response = call(HttpMethod.GET, APPLICATION_REFERENCE, ericApiKeyHeaders(), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        final JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("company_number").asString()).isEqualTo(COMPANY_NUMBER);
        assertThat(body.get("links").get("self").asString())
                .isEqualTo("/dissolution-request/" + APPLICATION_REFERENCE + "/payment");
        assertThat(body.get("items")).hasSize(1);
        assertThat(body.get("items").get(0).get("amount").asString()).isNotBlank();
    }

    @Test
    void getPayment_returnsNotFound_whenApplicationDoesNotExist() {
        final ResponseEntity<String> response = call(HttpMethod.GET, "UNKNOWNREF0001", ericApiKeyHeaders(), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPayment_returnsUnauthorised_whenEricIdentityIsMissing() {
        insertDissolution(ApplicationStatus.PENDING_PAYMENT);
        final HttpHeaders headers = ericApiKeyHeaders();
        headers.remove(EricConstants.ERIC_IDENTITY);

        final ResponseEntity<String> response = call(HttpMethod.GET, APPLICATION_REFERENCE, headers, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void patchPayment_paid_returnsNoContent_updatesDocumentAndSendsEmails() {
        insertDissolution(ApplicationStatus.PENDING_PAYMENT);

        final ResponseEntity<String> response = call(HttpMethod.PATCH, APPLICATION_REFERENCE, ericApiKeyHeaders(),
                paidBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        final Dissolution dissolution = findDissolution();
        assertThat(dissolution.getData().getApplication().getStatus()).isEqualTo(ApplicationStatus.PAID);
        assertThat(dissolution.getPaymentInformation().getReference()).isEqualTo(PAYMENT_REFERENCE);
        assertThat(dissolution.getPaymentInformation().getMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(dissolution.getPaymentInformation().getDateTime()).isNotNull();
        assertThat(dissolution.getSubmission()).isNotNull();

        verify(emailClient, atLeastOnce()).sendEmail(any());
    }

    @Test
    void patchPayment_notPaid_returnsNoContent_storesReferenceOnly() {
        insertDissolution(ApplicationStatus.PENDING_PAYMENT);

        final ResponseEntity<String> response = call(HttpMethod.PATCH, APPLICATION_REFERENCE, ericApiKeyHeaders(),
                "{\"status\":\"failed\",\"payment_reference\":\"FAILREF0000001\"}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        final Dissolution dissolution = findDissolution();
        assertThat(dissolution.getPaymentInformation().getReference()).isEqualTo("FAILREF0000001");
        assertThat(dissolution.getData().getApplication().getStatus()).isEqualTo(ApplicationStatus.PENDING_PAYMENT);

        verifyNoInteractions(emailClient);
    }

    @Test
    void patchPayment_returnsBadRequest_whenApplicationIsNotPendingPayment() {
        insertDissolution(ApplicationStatus.PENDING_APPROVAL);

        final ResponseEntity<String> response = call(HttpMethod.PATCH, APPLICATION_REFERENCE, ericApiKeyHeaders(),
                paidBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final Dissolution dissolution = findDissolution();
        assertThat(dissolution.getData().getApplication().getStatus()).isEqualTo(ApplicationStatus.PENDING_APPROVAL);
        assertThat(dissolution.getPaymentInformation().getReference()).isNull();

        verifyNoInteractions(emailClient);
    }

    @Test
    void patchPayment_returnsBadRequest_whenReferenceAndAccountNumberBothProvided() {
        insertDissolution(ApplicationStatus.PENDING_PAYMENT);

        final ResponseEntity<String> response = call(HttpMethod.PATCH, APPLICATION_REFERENCE, ericApiKeyHeaders(),
                "{\"status\":\"paid\",\"payment_reference\":\"" + PAYMENT_REFERENCE + "\","
                        + "\"paid_at\":" + System.currentTimeMillis() + ","
                        + "\"payment_method\":\"account\",\"account_number\":\"ACC12345\"}");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        final Dissolution dissolution = findDissolution();
        assertThat(dissolution.getData().getApplication().getStatus()).isEqualTo(ApplicationStatus.PENDING_PAYMENT);

        verifyNoInteractions(emailClient);
    }

    @Test
    void patchPayment_returnsNotFound_whenApplicationDoesNotExist() {
        final ResponseEntity<String> response = call(HttpMethod.PATCH, "UNKNOWNREF0001", ericApiKeyHeaders(),
                paidBody());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verifyNoInteractions(emailClient);
    }

    private void insertDissolution(ApplicationStatus status) {
        mongoTemplate.insert(DissolutionTestDataBuilder.aDissolution()
                .withApplicationReference(APPLICATION_REFERENCE)
                .withApplicationStatus(status)
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName("Companies House")
                .withCreatedByEmail("user@mail.com")
                .build());
    }

    private Dissolution findDissolution() {
        return mongoTemplate.findOne(
                Query.query(Criteria.where("data.application.reference").is(APPLICATION_REFERENCE)),
                Dissolution.class);
    }

    private String paidBody() {
        return "{\"status\":\"paid\",\"payment_reference\":\"" + PAYMENT_REFERENCE + "\","
                + "\"paid_at\":" + System.currentTimeMillis() + ","
                + "\"payment_method\":\"credit-card\"}";
    }

    private ResponseEntity<String> call(HttpMethod method, String reference, HttpHeaders headers, String jsonBody) {
        RestClient.RequestBodySpec request = RestClient.create()
                .method(method)
                .uri(baseUrl() + PAYMENT_PATH, reference)
                .headers(h -> h.addAll(headers));

        if (jsonBody != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).body(jsonBody);
        }

        return request.retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                })
                .toEntity(String.class);
    }
}
