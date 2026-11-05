package uk.gov.companieshouse.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class HealthcheckIT extends AbstractIntegrationTest {

    @Test
    void healthcheckReturnsUpWithoutAuthentication() {
        RestClient client = RestClient.create();

        ResponseEntity<String> response = client.get()
                .uri(baseUrl() + "/dissolution-api/healthcheck")
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}
