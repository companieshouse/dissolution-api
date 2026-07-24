package uk.gov.companieshouse.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.service.cost.CostService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CostController.class)
public class CostControllerTest {
    private static final String COST_URI = "/transactions/{transaction_id}/dissolution/{dissolution_id}/costs";
    private static final String TRANSACTION_ID = "123456789";
    private static final String DISSOLUTION_ID = "987654321";
    private static final String IDENTITY_HEADER_VALUE = "identity";

    @MockitoBean
    public CostService costService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidDissolutionIdAnd_whenGetCostCalled_thenReturnOk() throws Exception {
        HttpHeaders headers = createHttpHeaders();

        when(costService.getCosts(DISSOLUTION_ID)).thenReturn(new Cost());
        mockMvc.perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID).headers(headers))
                .andExpect(status().isOk());
    }

    @Test
    void givenNoIdentity_whenGetCostCalled_thenReturnsUnauthorized() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.remove(EricConstants.ERIC_IDENTITY);
        mockMvc.perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID).headers(headers))
                .andExpect(status().isUnauthorized());
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(EricConstants.ERIC_IDENTITY, IDENTITY_HEADER_VALUE);
        headers.add(EricConstants.ERIC_IDENTITY_TYPE, SecurityConstants.API_KEY_IDENTITY_TYPE);
        headers.add(EricConstants.ERIC_AUTHORISED_KEY_ROLES, SecurityConstants.INTERNAL_USER_ROLE);

        return headers;
    }


}
