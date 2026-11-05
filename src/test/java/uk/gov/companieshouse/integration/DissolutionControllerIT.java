package uk.gov.companieshouse.integration;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.fixtures.BarcodeFixtures;
import uk.gov.companieshouse.fixtures.CompanyOfficerFixtures;
import uk.gov.companieshouse.fixtures.CompanyProfileApiFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficersResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class DissolutionControllerIT extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "10001";
    private static final String DISSOLUTION_PATH = "/dissolution-request/" + COMPANY_NUMBER;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDissolutionRequestReturns201AndPersistsDraftDissolution() {
        stubOutboundCalls(profileResponse(CompanyProfileApiFixtures.generateCompanyProfileApi()), officersResponse());

        final ResponseEntity<String> response = post(ericUserHeaders(COMPANY_NUMBER),
                DissolutionFixtures.generateDissolutionCreateRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CREATED.value());
        final JsonNode body = objectMapper.readTree(response.getBody());
        assertThat(body.get("application_reference_number").asString()).isNotBlank();

        final List<Dissolution> stored = mongoTemplate.findAll(Dissolution.class);
        assertThat(stored).hasSize(1);
        assertThat(stored.getFirst().getCompany().getNumber()).isEqualTo(COMPANY_NUMBER);
        assertThat(stored.getFirst().getActive()).isTrue();
        assertThat(stored.getFirst().getCreatedBy().getUserId()).isEqualTo(ERIC_IDENTITY_VALUE);
    }

    @Test
    void createDissolutionRequestReturns409WhenOneAlreadyExists() {
        mongoTemplate.insert(DissolutionTestDataBuilder.aDissolution().withCompanyNumber(COMPANY_NUMBER).build());
        stubOutboundCalls(profileResponse(CompanyProfileApiFixtures.generateCompanyProfileApi()), officersResponse());

        final ResponseEntity<String> response = post(ericUserHeaders(COMPANY_NUMBER),
                DissolutionFixtures.generateDissolutionCreateRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(mongoTemplate.findAll(Dissolution.class)).hasSize(1);
    }

    @Test
    void createDissolutionRequestReturns401WithoutEricHeaders() {
        final ResponseEntity<String> response = post(new HttpHeaders(),
                DissolutionFixtures.generateDissolutionCreateRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
    }

    @Test
    void createDissolutionRequestReturns404WhenCompanyProfileNotFound() {
        stubOutboundCalls(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()), officersResponse());

        final ResponseEntity<String> response = post(ericUserHeaders(COMPANY_NUMBER),
                DissolutionFixtures.generateDissolutionCreateRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
    }

    @Test
    void getDissolutionRequestReturns200WithInProgressDissolution() {
        mongoTemplate.insert(DissolutionTestDataBuilder.aDissolution().withCompanyNumber(COMPANY_NUMBER).build());

        final ResponseEntity<String> response = get(ericUserHeaders(COMPANY_NUMBER));

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        assertThat(objectMapper.readTree(response.getBody()).get("company_number").asString()).isEqualTo(COMPANY_NUMBER);
    }

    @Test
    void getDissolutionRequestReturns404WhenNoneExists() {
        final ResponseEntity<String> response = get(ericUserHeaders(COMPANY_NUMBER));

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void patchDissolutionRequestReturns422WhenOfficerIdMissing() {
        final Map<String, Object> body = Map.of("has_approved", true, "ip_address", "127.0.0.1");

        final ResponseEntity<String> response = patch(ericUserHeaders(COMPANY_NUMBER), body);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT.value());
    }

    @Test
    void patchDissolutionRequestReturns404WhenNoneExists() {
        final Map<String, Object> body = Map.of("officer_id", "abc123", "has_approved", true, "ip_address", "127.0.0.1");

        final ResponseEntity<String> response = patch(ericUserHeaders(COMPANY_NUMBER), body);

        assertThat(response.getStatusCode().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private void stubOutboundCalls(MockResponse profileResponse, MockResponse officersResponse) {
        final MockResponse barcodeResponse = jsonResponse(BarcodeFixtures.generateBarcodeResponse());
        route(path -> {
            if (path.equals("/company/" + COMPANY_NUMBER)) {
                return profileResponse;
            }
            if (path.startsWith("/company/" + COMPANY_NUMBER + "/officers")) {
                return officersResponse;
            }
            if (path.equals("/")) {
                return barcodeResponse;
            }
            return new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value());
        });
    }

    private void route(Function<String, MockResponse> router) {
        try {
            mockWebServer().setDispatcher(new Dispatcher() {
                @Override
                public @NonNull MockResponse dispatch(@NonNull RecordedRequest request) {
                    return router.apply(request.getPath());
                }
            });
        } catch (Exception e) {
            throw new IllegalStateException("Unable to set dispatcher", e);
        }
    }

    private MockResponse profileResponse(CompanyProfileApi profile) {
        return jsonResponse(profile);
    }

    private MockResponse officersResponse() {
        final CompanyOfficersResponse officers = CompanyOfficerFixtures.generateCompanyOfficersResponse();
        officers.getItems().getFirst().setLinks(CompanyOfficerFixtures.generateCompanyOfficerLinks("abc123"));
        return jsonResponse(officers);
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

    private ResponseEntity<String> get(HttpHeaders headers) {
        return restClient().get()
                .uri(DISSOLUTION_PATH)
                .headers(h -> h.addAll(headers))
                .exchange((request, response) -> toEntity(response));
    }

    private ResponseEntity<String> post(HttpHeaders headers, Object body) {
        return restClient().post()
                .uri(DISSOLUTION_PATH)
                .headers(h -> h.addAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .body(toJson(body))
                .exchange((request, response) -> toEntity(response));
    }

    private ResponseEntity<String> patch(HttpHeaders headers, Object body) {
        return restClient().patch()
                .uri(DISSOLUTION_PATH)
                .headers(h -> h.addAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .body(toJson(body))
                .exchange((request, response) -> toEntity(response));
    }

    private RestClient restClient() {
        return RestClient.create(baseUrl());
    }

    private static ResponseEntity<String> toEntity(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response) throws IOException {
        return ResponseEntity.status(response.getStatusCode()).body(response.bodyTo(String.class));
    }

    private String toJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialise request body", e);
        }
    }
}
