package uk.gov.companieshouse.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.cost.CostService;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.model.Constants.ERIC_REQUEST_ID_KEY;

@WebMvcTest(CostController.class)
class CostControllerTest {
    private static final String COST_URI = "/transactions/{transaction_id}/dissolution/{dissolution_id}/costs";
    private static final String TRANSACTION_ID = "123456789";
    private static final String DISSOLUTION_ID = "987654321";
    private static final String IDENTITY_HEADER_VALUE = "identity";
    private static final String REQUEST_ID_HEADER_VALUE = "request-123";

    @MockitoBean
    public CostService costService;

    @MockitoBean
    public Logger logger;

    @MockitoBean
    public TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenValidDissolutionIdAnd_whenGetCostCalled_thenReturnOk() throws Exception {
        HttpHeaders headers = createHttpHeaders();

        when(transactionService.getTransaction(eq(TRANSACTION_ID), any())).thenReturn(new Transaction());

        when(costService.getCosts(DISSOLUTION_ID)).thenReturn(new Cost());
        mockMvc.perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID).headers(headers))
                .andExpect(status().isOk());
    }

    @Test
    void givenInvalidDissolutionIdAnd_whenGetCostCalled_thenReturnNotFound() throws Exception {
        HttpHeaders headers = createHttpHeaders();

        when(transactionService.getTransaction(eq(TRANSACTION_ID), any())).thenReturn(new Transaction());

        when(costService.getCosts(DISSOLUTION_ID)).thenThrow(new DissolutionNotFoundException());
        mockMvc.perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID).headers(headers))
                .andExpect(status().isNotFound());
    }

    @Test
    void givenServerDown_whenGetCostCalled_thenReturnInternalServerError() throws Exception {
        HttpHeaders headers = createHttpHeaders();

        when(transactionService.getTransaction(eq(TRANSACTION_ID), any())).thenReturn(new Transaction());

        when(costService.getCosts(DISSOLUTION_ID)).thenThrow(new RuntimeException("Server down"));
        mockMvc.perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID).headers(headers))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void givenERICIdentityNotProvided_whenGetCostCalled_thenReturnUnauthorized() throws Exception {
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
        headers.add(ERIC_REQUEST_ID_KEY, REQUEST_ID_HEADER_VALUE);

        return headers;
    }
}