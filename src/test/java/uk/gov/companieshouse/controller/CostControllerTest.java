package uk.gov.companieshouse.controller;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.cost.CostService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;
import static uk.gov.companieshouse.model.Constants.HEADER_ERIC_REQUEST_ID;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_KEY;

@WebMvcTest(CostController.class)
class CostControllerTest {
    private static final String COST_URI = "/transactions/{transaction_id}/dissolution/{dissolution_id}/costs";
    private static final String DISSOLUTION_ID = "987654321";
    private static final String IDENTITY_HEADER_VALUE = "identity";
    private static final String REQUEST_ID_HEADER_VALUE = "request-123";
    private static final String PASS_THROUGH_HEADER = "545345345";
    private static final String ERIC_ACCESS_TOKEN_HEADER = "ERIC-Access-Token";

    @MockitoBean
    public CostService costService;

    @MockitoBean
    public Logger logger;

    @MockitoBean
    public TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    private Transaction transaction;

    @BeforeEach
    void setup() {
        transaction = TransactionFixtures.generateClosedTransaction();
        when(transactionService.getTransaction(TRANSACTION_ID, PASS_THROUGH_HEADER)).thenReturn(transaction);
    }

    @Test
    void givenValidDissolutionIdAnd_whenGetCostCalled_thenReturnOk() throws Exception {
        when(costService.getCosts(isA(Transaction.class), eq(DISSOLUTION_ID))).thenReturn(new Cost());
        mockMvc.perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isOk());
    }

    @Test
    void givenInvalidDissolutionIdAnd_whenGetCostCalled_thenReturnNotFound() throws Exception {
        when(costService.getCosts(isA(Transaction.class), eq(DISSOLUTION_ID))).thenThrow(new DissolutionNotFoundException());
        mockMvc.perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCost_returnsBadRequest_ifDissolutionNotLinkedToTransaction() throws Exception {
        when(costService.getCosts(isA(Transaction.class), eq(DISSOLUTION_ID)))
                .thenThrow(new DissolutionNotLinkedToTransactionException("dissolution not linked to transaction"));

        mockMvc.perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenServerDown_whenGetCostCalled_thenReturnInternalServerError() throws Exception {
        when(costService.getCosts(isA(Transaction.class), eq(DISSOLUTION_ID))).thenThrow(new RuntimeException("Server down"));
        mockMvc.perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
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
        headers.add(ERIC_ACCESS_TOKEN_HEADER, PASS_THROUGH_HEADER);
        headers.add(HEADER_ERIC_REQUEST_ID, REQUEST_ID_HEADER_VALUE);

        return headers;
    }
}