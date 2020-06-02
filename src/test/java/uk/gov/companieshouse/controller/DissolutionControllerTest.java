package uk.gov.companieshouse.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class DissolutionControllerTest {

    @InjectMocks
    DissolutionController controller;

    @Test
    public void When_HealthcheckEndpointIsCalled_Expect_200() {
        ResponseEntity<String> response = controller.healthcheck();

        assertThat(response.getStatusCode()).isEqualTo(OK);
    }
}
