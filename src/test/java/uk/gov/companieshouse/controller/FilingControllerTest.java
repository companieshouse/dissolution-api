package uk.gov.companieshouse.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.TransactionNotFoundException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.transaction.FilingService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;
import static uk.gov.companieshouse.model.Constants.HEADER_ERIC_REQUEST_ID;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_KEY;

@WebMvcTest(FilingController.class)
class FilingControllerTest {

    private static final String FILING_URI = "/private/transactions/{transaction_id}/dissolution/{dissolution_id}/filings";
    private static final String DISSOLUTION_ID = "12345678";
    private static final String ERIC_REQUEST_ID = "XaBcDeF12345";
    private static final String PASS_THROUGH_HEADER = "545345345";
    private static final String ERIC_ACCESS_TOKEN_HEADER = "ERIC-Access-Token";
    private static final String IDENTITY_HEADER_VALUE = "identity";

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private FilingService filingService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private Transaction transaction;

    @BeforeEach
    void setup() {
        transaction = TransactionFixtures.generateClosedTransaction();
        when(transactionService.getTransaction(TRANSACTION_ID)).thenReturn(transaction);
    }

    @Test
    void getFiling_returnsUnauthorised_ifEricIdentityIsNotProvided() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.remove(EricConstants.ERIC_IDENTITY);

        mockMvc
                .perform(
                        get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                .headers(headers)
                                .requestAttr(TRANSACTION_KEY, transaction)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getFiling_returnsForbidden_ifEricIdentityTypeIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_IDENTITY_TYPE, "some-incorrect-identity-type");

        mockMvc
                .perform(
                        get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                .headers(headers)
                                .requestAttr(TRANSACTION_KEY, transaction)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getFiling_returnsForbidden_ifEricAuthorisedKeyRolesIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_AUTHORISED_KEY_ROLES, "some-incorrect-authorised-key-roles-value");

        mockMvc
                .perform(
                        get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                .headers(headers)
                                .requestAttr(TRANSACTION_KEY, transaction)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getFiling_returnsNotFound_ifDissolutionNotFound() throws Exception {
        when(filingService.generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID), eq(PASS_THROUGH_HEADER)))
                .thenThrow(new DissolutionNotFoundException("dissolution not found"));

        mockMvc
                .perform(
                        get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getFiling_returnsNotFound_ifTransactionNotFound() throws Exception {
        when(transactionService.getTransaction(TRANSACTION_ID)).thenThrow(TransactionNotFoundException.class);

        mockMvc
                .perform(
                        get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isNotFound());

        verify(filingService, never()).generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID), eq(PASS_THROUGH_HEADER));
    }

    @Test
    void getFiling_returnsConflict_ifTransactionIsNotClosed() throws Exception {
        transaction.setStatus(TransactionStatus.OPEN);

        mockMvc
                .perform(
                        get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                )
                .andExpect(status().isConflict());

        verify(filingService, never()).generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID), eq(PASS_THROUGH_HEADER));
    }

    @Test
    void getFiling_returnsBadRequest_ifDissolutionNotLinkedToTransaction() throws Exception {
        when(filingService.generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID), eq(PASS_THROUGH_HEADER)))
                .thenThrow(new DissolutionNotLinkedToTransactionException("dissolution not linked to transaction"));

        mockMvc
                .perform(
                        get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFiling_returnsInternalServerError_ifExceptionOccursWhenGeneratingFiling() throws Exception {
        when(filingService.generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID), eq(PASS_THROUGH_HEADER)))
                .thenThrow(new RuntimeException("Some error occurred while generating dissolution filing"));

        mockMvc
                .perform(
                        get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                )
                .andExpect(status().isInternalServerError());

        verify(filingService).generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID), eq(PASS_THROUGH_HEADER));
    }

    @Test
    void getFiling_returnsFilingData_returnsOk() throws Exception {
        FilingApi filing = new FilingApi();
        filing.setDescription("12345678");
        FilingApi[] response = new FilingApi[]{filing};

        when(filingService.generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID), eq(PASS_THROUGH_HEADER)))
                .thenReturn(filing);

        mockMvc
                .perform(
                        get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(EricConstants.ERIC_IDENTITY, IDENTITY_HEADER_VALUE);
        headers.add(EricConstants.ERIC_IDENTITY_TYPE, SecurityConstants.API_KEY_IDENTITY_TYPE);
        headers.add(EricConstants.ERIC_AUTHORISED_KEY_ROLES, SecurityConstants.INTERNAL_USER_ROLE);
        headers.add(ERIC_ACCESS_TOKEN_HEADER, PASS_THROUGH_HEADER);
        headers.add(HEADER_ERIC_REQUEST_ID, ERIC_REQUEST_ID);

        return headers;
    }

    private <T> String asJsonString(T body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
