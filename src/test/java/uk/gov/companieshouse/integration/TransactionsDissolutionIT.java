package uk.gov.companieshouse.integration;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.fixtures.CompanyOfficerFixtures;
import uk.gov.companieshouse.fixtures.CompanyProfileApiFixtures;
import uk.gov.companieshouse.fixtures.CreatedByTestDataBuilder;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficersResponse;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;

class TransactionsDissolutionIT extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "10001";
    private static final String TRANSACTION_ID = TransactionFixtures.TRANSACTION_ID;
    private static final String DISSOLUTION_PATH =
            "/company/" + COMPANY_NUMBER + "/transactions/" + TRANSACTION_ID + "/dissolution";

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDraftReturns201AndPersistsDraftDissolution() {
        stubOutboundCalls(linkedTransaction(TransactionStatus.OPEN), "abc123");

        final ResponseEntity<String> response = send("POST", DISSOLUTION_PATH, ericUserHeaders(COMPANY_NUMBER), null);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CREATED.value());

        final List<Dissolution> stored = mongoTemplate.findAll(Dissolution.class);
        assertThat(stored).hasSize(1);
        assertThat(stored.getFirst().getStatus()).isEqualTo(DissolutionStatus.DRAFT);
        assertThat(stored.getFirst().getCompany().getNumber()).isEqualTo(COMPANY_NUMBER);
        assertThat(stored.getFirst().getTransactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(stored.getFirst().getCreatedBy().getUserId()).isEqualTo(ERIC_IDENTITY_VALUE);
    }

    @Test
    void createDraftReturns401WithoutEricHeaders() {
        stubOutboundCalls(linkedTransaction(TransactionStatus.OPEN), "abc123");

        final ResponseEntity<String> response = send("POST", DISSOLUTION_PATH, new HttpHeaders(), null);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
    }

    @Test
    void createDraftReturns409WhenTransactionIsNotOpen() {
        stubOutboundCalls(linkedTransaction(TransactionStatus.CLOSED), "abc123");

        final ResponseEntity<String> response = send("POST", DISSOLUTION_PATH, ericUserHeaders(COMPANY_NUMBER), null);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
    }

    @Test
    void approveReturns204AndRecordsDirectorApproval() {
        insertDissolution(DissolutionStatus.PENDING);
        stubOutboundCalls(linkedTransaction(TransactionStatus.OPEN), "abc123", "def456");

        final ResponseEntity<String> response = send("PATCH", DISSOLUTION_PATH + "/approve",
                ericUserHeaders(COMPANY_NUMBER), toJson(approvalBody()));

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NO_CONTENT.value());

        final Dissolution stored = mongoTemplate.findAll(Dissolution.class).getFirst();
        assertThat(findDirector(stored, "abc123").hasDirectorApproval()).isTrue();
        assertThat(findDirector(stored, "def456").hasDirectorApproval()).isFalse();
    }

    @Test
    void approveReturns404WhenNoPendingDissolutionExists() {
        stubOutboundCalls(linkedTransaction(TransactionStatus.OPEN), "abc123", "def456");

        final ResponseEntity<String> response = send("PATCH", DISSOLUTION_PATH + "/approve",
                ericUserHeaders(COMPANY_NUMBER), toJson(approvalBody()));

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void initiationReturns204SetsDissolutionPendingAndNotifiesSignatories() {
        insertDissolution(DissolutionStatus.DRAFT);
        stubOutboundCalls(linkedTransaction(TransactionStatus.OPEN), "abc123", "def456");

        final Map<String, Object> body = Map.of("directors", List.of(
                Map.of("officer_id", "abc123", "email", "john@doe.com"),
                Map.of("officer_id", "def456", "email", "jane@doe.com")));
        final ResponseEntity<String> response = send("POST", DISSOLUTION_PATH + "/initiation",
                ericUserHeaders(COMPANY_NUMBER), toJson(body));

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NO_CONTENT.value());

        final Dissolution stored = mongoTemplate.findAll(Dissolution.class).getFirst();
        assertThat(stored.getStatus()).isEqualTo(DissolutionStatus.PENDING);
        verify(emailClient, atLeastOnce()).sendEmail(any());
    }

    @Test
    void initiationReturns400WhenSelectedDirectorIsNotAnOfficer() {
        insertDissolution(DissolutionStatus.DRAFT);
        stubOutboundCalls(linkedTransaction(TransactionStatus.OPEN), "abc123", "def456");

        final Map<String, Object> body = Map.of("directors", List.of(
                Map.of("officer_id", "unknown999", "email", "nobody@doe.com")));
        final ResponseEntity<String> response = send("POST", DISSOLUTION_PATH + "/initiation",
                ericUserHeaders(COMPANY_NUMBER), toJson(body));

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());

        final Dissolution stored = mongoTemplate.findAll(Dissolution.class).getFirst();
        assertThat(stored.getStatus()).isEqualTo(DissolutionStatus.DRAFT);
    }

    @Test
    void resendSignatoryNotificationReturns200WhenApplicant() {
        insertDissolution(DissolutionStatus.PENDING);
        stubOutboundCalls(linkedTransaction(TransactionStatus.OPEN), "abc123", "def456");

        final ResponseEntity<String> response = send("POST",
                DISSOLUTION_PATH + "/signatories/abc123/signature-notification",
                ericUserHeaders(COMPANY_NUMBER), null);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        verify(emailClient, atLeastOnce()).sendEmail(any());
    }

    @Test
    void updateSignatoryReturns200AndPersistsChangedDetails() {
        insertDissolution(DissolutionStatus.PENDING);
        stubOutboundCalls(linkedTransaction(TransactionStatus.OPEN), "abc123", "def456");

        final Map<String, Object> body = Map.of(
                "email", "jane.doe@mail.com",
                "on_behalf_name", "Jane Doe");
        final ResponseEntity<String> response = send("PATCH", DISSOLUTION_PATH + "/signatories/abc123",
                ericUserHeaders(COMPANY_NUMBER), toJson(body));

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());

        final DissolutionDirector updated = findDirector(mongoTemplate.findAll(Dissolution.class).getFirst(), "abc123");
        assertThat(updated.getEmail()).isEqualTo("jane.doe@mail.com");
        assertThat(updated.getOnBehalfName()).isEqualTo("Jane Doe");
    }

    private void stubOutboundCalls(Transaction transaction, String... officerIds) {
        mockWebServer().setDispatcher(new Dispatcher() {
            @Override
            public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                final String method = request.getMethod();
                assert request.getPath() != null;
                final String path = request.getPath().split("\\?")[0];

                if ("PATCH".equals(method) && path.startsWith("/private/transactions/")) {
                    return new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value());
                }
                if ("GET".equals(method) && path.equals("/transactions/" + TRANSACTION_ID)) {
                    return jsonResponse(transaction);
                }
                if ("GET".equals(method) && path.equals("/company/" + COMPANY_NUMBER)) {
                    return jsonResponse(CompanyProfileApiFixtures.generateCompanyProfileApi());
                }
                if ("GET".equals(method) && path.equals("/company/" + COMPANY_NUMBER + "/officers")) {
                    return jsonResponse(officersResponse(officerIds));
                }
                return new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value());
            }
        });
    }

    private Transaction linkedTransaction(TransactionStatus status) {
        return TransactionTestDataBuilder.aTransaction()
                .withStatus(status)
                .withCompanyNumber(COMPANY_NUMBER)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, COMPANY_NUMBER))
                .build();
    }

    private void insertDissolution(DissolutionStatus status) {
        mongoTemplate.insert(DissolutionTestDataBuilder.aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withTransactionId(TRANSACTION_ID)
                .withCreatedBy(CreatedByTestDataBuilder.aCreatedBy().withUserId(AbstractIntegrationTest.ERIC_IDENTITY_VALUE))
                .withDirectors(DissolutionFixtures.generateDissolutionDirectorList())
                .withStatus(status)
                .withActive(false)
                .build());
    }

    private DissolutionDirector findDirector(Dissolution dissolution, String officerId) {
        return dissolution.findSignatory(officerId).orElseThrow();
    }

    private Map<String, Object> approvalBody() {
        return Map.of("officer_id", "abc123", "has_approved", true, "ip_address", "127.0.0.1");
    }

    private CompanyOfficersResponse officersResponse(String... officerIds) {
        final CompanyOfficersResponse response = new CompanyOfficersResponse();
        final List<CompanyOfficer> items = new ArrayList<>();
        for (String officerId : officerIds) {
            final CompanyOfficer officer = CompanyOfficerFixtures.generateCompanyOfficer();
            officer.setLinks(CompanyOfficerFixtures.generateCompanyOfficerLinks(officerId));
            items.add(officer);
        }
        response.setItems(items);
        return response;
    }

    private MockResponse jsonResponse(Object body) {
        try {
            return new MockResponse()
                    .setResponseCode(HttpStatus.OK.value())
                    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                    .setBody(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialise mock response", e);
        }
    }

    private ResponseEntity<String> send(String method, String path, HttpHeaders headers, String jsonBody) {
        RestClient.RequestBodySpec request = restClient().method(HttpMethod.valueOf(method))
                .uri(path)
                .headers(h -> h.addAll(headers));
        if (jsonBody != null) {
            request = request.contentType(MediaType.APPLICATION_JSON).body(jsonBody);
        }
        return request.exchange((req, res) -> toEntity(res));
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialise request body", e);
        }
    }
}
