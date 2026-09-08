package uk.gov.companieshouse.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import uk.gov.companieshouse.exception.InvalidTransactionStateException;
import uk.gov.companieshouse.exception.TransactionNotFoundException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;
import uk.gov.companieshouse.mapper.CostMapper;
import uk.gov.companieshouse.mapper.ValidationStatusResponseMapper;
import uk.gov.companieshouse.model.domain.DissolutionCost;
import uk.gov.companieshouse.model.domain.ValidationResult;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.cost.CostService;
import uk.gov.companieshouse.service.transaction.FilingService;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.HEADER_ERIC_REQUEST_ID;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_KEY;

@WebMvcTest(FilingController.class)
@Import({ValidationStatusResponseMapper.class, CostMapper.class})
class FilingControllerTest {

    private static final String FILING_URI = "/private/transactions/{transaction_id}/dissolution/{dissolution_id}/filings";
    private static final String VALIDATION_STATUS_URI = "/transactions/{transaction_id}/dissolution/{dissolution_id}/validation-status";
    private static final String COST_URI = "/transactions/{transaction_id}/dissolution/{dissolution_id}/costs";
    private static final String DISSOLUTION_ID = "12345678";
    private static final String ERIC_REQUEST_ID = "XaBcDeF12345";
    private static final String PASS_THROUGH_HEADER = "545345345";
    private static final String ERIC_ACCESS_TOKEN_HEADER = "ERIC-Access-Token";
    private static final String IDENTITY_HEADER_VALUE = "identity";

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private FilingService filingService;

    @MockitoBean
    private CostService costService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private Transaction transaction;

    @BeforeEach
    void setup() {
        transaction = TransactionTestDataBuilder.aTransaction()
                .withId(TRANSACTION_ID)
                .withStatus(TransactionStatus.CLOSED)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, DISSOLUTION_ID))
                .build();
        when(transactionService.getTransaction(TRANSACTION_ID)).thenReturn(transaction);
    }

    @Nested
    class GetFiling {

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
            when(filingService.generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
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

            verify(filingService, never()).generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID));
        }

        @Test
        void getFiling_returnsConflict_ifTransactionIsNotClosed() throws Exception {
            transaction.setStatus(TransactionStatus.OPEN);
            when(filingService.generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
                    .thenThrow(new InvalidTransactionStateException("transaction is not closed"));

            mockMvc
                    .perform(
                            get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isConflict());
        }

        @Test
        void getFiling_returnsBadRequest_ifDissolutionNotLinkedToTransaction() throws Exception {
            when(filingService.generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
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
            when(filingService.generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
                    .thenThrow(new RuntimeException("Some error occurred while generating dissolution filing"));

            mockMvc
                    .perform(
                            get(FILING_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isInternalServerError());

            verify(filingService).generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID));
        }

        @Test
        void getFiling_returnsFilingData_returnsOk() throws Exception {
            FilingApi filing = new FilingApi();
            filing.setDescription("12345678");
            FilingApi[] response = new FilingApi[]{filing};

            when(filingService.generateDissolutionFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
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
    }

    @Nested
    class GetValidationStatus {

        @Test
        void when_no_dissolution_found_for_id_then_NOT_FOUND_returned() throws Exception {
            when(filingService.validateForFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
                    .thenThrow(new DissolutionNotFoundException("dissolution not found"));

            mockMvc
                    .perform(
                            get(VALIDATION_STATUS_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void when_dissolution_not_linked_to_transaction_then_BAD_REQUEST_returned() throws Exception {
            when(filingService.validateForFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
                    .thenThrow(new DissolutionNotLinkedToTransactionException("dissolution not linked to transaction"));

            mockMvc
                    .perform(
                            get(VALIDATION_STATUS_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void when_unexpected_exception_occurs_then_INTERNAL_SERVER_ERROR_returned() throws Exception {
            when(filingService.validateForFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
                    .thenThrow(new RuntimeException("Some error occurred while validating dissolution"));

            mockMvc
                    .perform(
                            get(VALIDATION_STATUS_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isInternalServerError());

            verify(filingService).validateForFiling(isA(Transaction.class), eq(DISSOLUTION_ID));
        }

        @Test
        void when_dissolution_is_valid_then_OK_returned_with_valid_response() throws Exception {
            when(filingService.validateForFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
                    .thenReturn(new ValidationResult());

            mockMvc
                    .perform(
                            get(VALIDATION_STATUS_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().json("{\"is_valid\":true,\"errors\":[]}"));
        }

        @Test
        void when_dissolution_is_invalid_then_OK_returned_with_errors() throws Exception {
            var validationResult = new ValidationResult();
            validationResult.addError("Dissolution status is PENDING, expected SUBMITTED");

            when(filingService.validateForFiling(isA(Transaction.class), eq(DISSOLUTION_ID)))
                    .thenReturn(validationResult);

            mockMvc
                    .perform(
                            get(VALIDATION_STATUS_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().json(
                            "{\"is_valid\":false,\"errors\":[{\"error\":\"Dissolution status is PENDING, expected SUBMITTED\"}]}"));
        }
    }

    @Nested
    class GetCosts {

        @Test
        void when_dissolution_is_valid_then_OK_returned() throws Exception {
            var dissolutionCost = new DissolutionCost("13", "ACME LTD", "12345678", "dissolution");
            when(costService.getCosts(isA(Transaction.class), eq(DISSOLUTION_ID))).thenReturn(dissolutionCost);

            mockMvc
                    .perform(
                            get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().json("[{\"amount\":\"13\",\"product_type\":\"dissolution\"}]"));
        }

        @Test
        void when_dissolution_not_found_then_NOT_FOUND_returned() throws Exception {
            when(costService.getCosts(isA(Transaction.class), eq(DISSOLUTION_ID))).thenThrow(new DissolutionNotFoundException());

            mockMvc
                    .perform(
                            get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        void when_dissolution_not_linked_to_transaction_then_BAD_REQUEST_returned() throws Exception {
            when(costService.getCosts(isA(Transaction.class), eq(DISSOLUTION_ID)))
                    .thenThrow(new DissolutionNotLinkedToTransactionException("dissolution not linked to transaction"));

            mockMvc
                    .perform(
                            get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isBadRequest());
        }

        @Test
        void when_unexpected_exception_occurs_then_INTERNAL_SERVER_ERROR_returned() throws Exception {
            when(costService.getCosts(isA(Transaction.class), eq(DISSOLUTION_ID))).thenThrow(new RuntimeException("Server down"));

            mockMvc
                    .perform(
                            get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID)
                                    .headers(createHttpHeaders())
                                    .requestAttr(TRANSACTION_KEY, transaction)
                    )
                    .andExpect(status().isInternalServerError());
        }

        @Test
        void when_eric_identity_not_provided_then_UNAUTHORIZED_returned() throws Exception {
            HttpHeaders headers = createHttpHeaders();
            headers.remove(EricConstants.ERIC_IDENTITY);

            mockMvc
                    .perform(get(COST_URI, TRANSACTION_ID, DISSOLUTION_ID).headers(headers))
                    .andExpect(status().isUnauthorized());
        }
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
