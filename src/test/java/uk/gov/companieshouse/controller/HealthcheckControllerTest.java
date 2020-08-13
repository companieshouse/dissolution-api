package uk.gov.companieshouse.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(HealthcheckController.class)
public class HealthcheckControllerTest {

    private static final String HEALTHCHECK_URI = "/dissolution-request/healthcheck";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void isHealthy_returnsOk() throws Exception {
        mockMvc
            .perform(get(HEALTHCHECK_URI))
            .andExpect(status().isOk());
    }
}
