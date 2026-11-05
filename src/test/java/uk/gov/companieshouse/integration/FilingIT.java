package uk.gov.companieshouse.integration;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.fixtures.DirectorApprovalTestDataBuilder.aDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionCertificate;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;
import static uk.gov.companieshouse.model.Constants.DISSOLUTION_BASE_URI_PATTERN;

class FilingIT extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String DISSOLUTION_ID = "dissolution-id-1";
    private static final String TRANSACTION_PATH = "/transactions/" + TRANSACTION_ID;
    private static final String PAYMENT_LINK_PATH = "/transactions/" + TRANSACTION_ID + "/payment";
    private static final String PAYMENT_REFERENCE = "ref-123";
    private static final String PAYMENT_METHOD = "credit-card";
    private static final String DISSOLUTION_RESOURCE_KIND = "dissolution#ds01";

    private static final String FILINGS_PATH =
            "/private/company/" + COMPANY_NUMBER + "/transactions/" + TRANSACTION_ID + "/dissolution/filings";
    private static final String VALIDATION_STATUS_PATH =
            "/company/" + COMPANY_NUMBER + "/transactions/" + TRANSACTION_ID + "/dissolution/validation-status";
    private static final String COSTS_PATH =
            "/company/" + COMPANY_NUMBER + "/transactions/" + TRANSACTION_ID + "/dissolution/costs";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void when_dissolution_is_closed_and_linked_then_filing_is_returned_with_payment_details() {
        seedDissolution(submittedDissolution());
        stubTransactionAndPayment("closed");

        final ResponseEntity<String> response = get(FILINGS_PATH, ericApiKeyHeaders());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        final JsonNode filings = objectMapper.readTree(response.getBody());
        assertThat(filings.isArray()).isTrue();
        assertThat(filings).hasSize(1);
        final JsonNode filing = filings.get(0);
        assertThat(filing.get("kind").asString()).isEqualTo("dissolution#ds01");
        assertThat(filing.get("description").asString()).isEqualTo("Dissolution filing");
        assertThat(filing.get("cost").asString()).isEqualTo("13");
        assertThat(filing.get("data").get("payment_reference").asString()).isEqualTo(PAYMENT_REFERENCE);
        assertThat(filing.get("data").get("payment_method").asString()).isEqualTo(PAYMENT_METHOD);

        assertThat(mongoTemplate.findById(DISSOLUTION_ID, Dissolution.class))
                .isNotNull()
                .satisfies(saved -> assertThat(saved.getStatus()).isEqualTo(DissolutionStatus.SUBMITTED));
    }

    @Test
    void when_request_has_no_eric_identity_then_unauthorised_returned() {
        seedDissolution(submittedDissolution());
        stubTransactionAndPayment("closed");

        final ResponseEntity<String> response = get(FILINGS_PATH, new HttpHeaders());

        assertThat(response.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void when_dissolution_does_not_exist_then_not_found_returned_for_filings() {
        stubTransactionAndPayment("closed");

        final ResponseEntity<String> response = get(FILINGS_PATH, ericApiKeyHeaders());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void when_transaction_is_not_closed_then_conflict_returned_for_filings() {
        seedDissolution(submittedDissolution());
        stubTransactionAndPayment("open");

        final ResponseEntity<String> response = get(FILINGS_PATH, ericApiKeyHeaders());

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void when_dissolution_is_submitted_and_all_directors_approved_then_validation_status_is_valid() {
        seedDissolution(submittedDissolution());
        stubTransactionAndPayment("open");

        final ResponseEntity<String> response = get(VALIDATION_STATUS_PATH, ericUserHeaders(COMPANY_NUMBER));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        final JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("is_valid").asBoolean()).isTrue();
        assertThat(body.get("errors")).isEmpty();
    }

    @Test
    void when_dissolution_is_not_submitted_then_validation_status_reports_errors() {
        seedDissolution(aDissolution()
                .withId(DISSOLUTION_ID)
                .withTransactionId(TRANSACTION_ID)
                .withCompanyNumber(COMPANY_NUMBER)
                .withStatus(DissolutionStatus.PENDING)
                .withDirectors(aDissolutionDirector().withDirectorApproval(aDirectorApproval()))
                .build());
        stubTransactionAndPayment("open");

        final ResponseEntity<String> response = get(VALIDATION_STATUS_PATH, ericUserHeaders(COMPANY_NUMBER));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        final JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("is_valid").asBoolean()).isFalse();
        assertThat(body.get("errors").get(0).get("error").asString())
                .isEqualTo("Dissolution status is PENDING, expected SUBMITTED");
    }

    @Test
    void when_dissolution_is_linked_then_costs_are_returned() {
        seedDissolution(submittedDissolution());
        stubTransactionAndPayment("open");

        final ResponseEntity<String> response = get(COSTS_PATH, ericApiKeyHeaders());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        final JsonNode costs = objectMapper.readTree(response.getBody());
        assertThat(costs).hasSize(1);
        assertThat(costs.get(0).get("amount").asString()).isEqualTo("13");
        assertThat(costs.get(0).get("product_type").asString()).isEqualTo("ds01");
    }

    @Test
    void when_dissolution_does_not_exist_then_not_found_returned_for_costs() {
        stubTransactionAndPayment("open");

        final ResponseEntity<String> response = get(COSTS_PATH, ericApiKeyHeaders());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    private Dissolution submittedDissolution() {
        final Dissolution dissolution = aDissolution()
                .withId(DISSOLUTION_ID)
                .withTransactionId(TRANSACTION_ID)
                .withCompanyNumber(COMPANY_NUMBER)
                .withStatus(DissolutionStatus.SUBMITTED)
                .withDirectors(aDissolutionDirector().withDirectorApproval(aDirectorApproval()))
                .build();
        dissolution.setCertificate(generateDissolutionCertificate());
        return dissolution;
    }

    private void seedDissolution(final Dissolution dissolution) {
        mongoTemplate.insert(dissolution);
    }

    /**
     * Stubs the transaction lookup (used by the transaction interceptor) and, for filings, the payment lookups.
     */
    private void stubTransactionAndPayment(final String transactionStatus) {
        final String dissolutionUri = String.format(DISSOLUTION_BASE_URI_PATTERN, COMPANY_NUMBER, TRANSACTION_ID);
        final String transactionJson = objectMapper.writeValueAsString(Map.of(
                "id", TRANSACTION_ID,
                "status", transactionStatus,
                "company_number", COMPANY_NUMBER,
                "company_name", "Companies House",
                "resources", Map.of(dissolutionUri, Map.of(
                        "kind", DISSOLUTION_RESOURCE_KIND,
                        "links", Map.of("resource", dissolutionUri))),
                "links", Map.of("payment", PAYMENT_LINK_PATH)));
        final String paymentJson = objectMapper.writeValueAsString(Map.of("payment_reference", PAYMENT_REFERENCE));
        final String paymentSessionJson = objectMapper.writeValueAsString(Map.of("payment_method", PAYMENT_METHOD));

        mockWebServer().setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(final @NonNull RecordedRequest request) {
                assert request.getPath() != null;
                return switch (request.getPath()) {
                    case TRANSACTION_PATH -> json(transactionJson);
                    case PAYMENT_LINK_PATH -> json(paymentJson);
                    case "/payments/" + PAYMENT_REFERENCE -> json(paymentSessionJson);
                    default -> new MockResponse().setResponseCode(404);
                };
            }
        });
    }

    private static MockResponse json(final String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private ResponseEntity<String> get(final String path, final HttpHeaders headers) {
        return restClient().get()
                .uri(path)
                .headers(h -> h.addAll(headers))
                .exchange((request, response) -> toEntity(response));
    }
}
