package uk.gov.companieshouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsPaymentGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionPayment;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.exception.TransactionNotFoundException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.FilingTestDataBuilder.aFiling;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;
import static uk.gov.companieshouse.fixtures.TransactionTestDataBuilder.aTransaction;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String TRANSACTIONS_URL = "/transactions/";
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String PAYMENT_URI = String.format("/transactions/%s/payment", TRANSACTION_ID);

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private ApiClient apiClient;

    @Mock
    private TransactionsResourceHandler transactionsResourceHandler;

    @Mock
    private TransactionsGet transactionsGet;

    @Mock
    private TransactionsPaymentGet transactionsPaymentGet;

    @Mock
    private ApiResponse<Transaction> apiGetResponse;

    @Mock
    private ApiResponse<TransactionPayment> apiGetPaymentResponse;

    @InjectMocks
    private TransactionService transactionService;

    @Nested
    @DisplayName("GET /transactions/{transaction_id}")
    class GetTransactionDetails {
        @BeforeEach
        void initialize() throws IOException {
            when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);
            when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
            when(transactionsResourceHandler.get(TRANSACTIONS_URL + TRANSACTION_ID)).thenReturn(transactionsGet);
        }

        @Test
        void getTransaction_returnsTransactionData_ifTransactionExists() throws IOException, URIValidationException {
            final var transaction = TransactionFixtures.generateTransaction();

            when(transactionsGet.execute()).thenReturn(apiGetResponse);
            when(apiGetResponse.getData()).thenReturn(transaction);

            var response = transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER);
            assertEquals(transaction, response);
        }

        @Test
        void getTransaction_throwsNotFoundException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(404, "404 Not Found"));
            final var exception = assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
            assertThat(exception.getMessage(),
                    is("No transaction found with id " + TRANSACTION_ID));
        }

        @Test
        void getTransaction_throwsServiceException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(400, "400 Bad Request"));
            assertThrows(ServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
        }

        @Test
        void getTransaction_throwsServiceException_ifUriValidationExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(new URIValidationException("ERROR"));
            assertThrows(ServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
        }

        @Test
        void getTransaction_throwsServiceException_ifIOExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));
            assertThrows(ServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class hasVerdictBeenReached {

        @BeforeEach
        void initialize() throws IOException, URIValidationException {
            when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);
            when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
            when(transactionsResourceHandler.get(TRANSACTIONS_URL + TRANSACTION_ID)).thenReturn(transactionsGet);
            when(transactionsGet.execute()).thenReturn(apiGetResponse);
        }

        @ParameterizedTest(name = "when {0} filing is {1} then {2}")
        @CsvSource({
                "dissolution#ds01,   ACCEPTED, true",
                "dissolution#llds01, ACCEPTED, true",
                "dissolution#ds01,   REJECTED, true",
                "dissolution#llds01, REJECTED, true",
                "dissolution#ds01,   PROCESSING,  false",
                "accounts#abridged,  ACCEPTED, false"
        })
        void verdict_reached_scenarios(String filingType, FilingStatus status, boolean expectedVerdict) {
            when(apiGetResponse.getData()).thenReturn(aTransaction()
                    .withFiling(aFiling().withType(filingType).withStatus(status))
                    .build());

            assertThat(transactionService.hasVerdictBeenReached(TRANSACTION_ID, PASSTHROUGH_HEADER)).isEqualTo(expectedVerdict);
        }

        @Test
        void when_no_dissolution_filing_exists_then_false() {
            when(apiGetResponse.getData()).thenReturn(aTransaction()
                    .withNoFilings()
                    .build());

            assertThat(transactionService.hasVerdictBeenReached(TRANSACTION_ID, PASSTHROUGH_HEADER)).isFalse();
        }

        @Test
        void when_filings_is_null_then_false() {
            when(apiGetResponse.getData()).thenReturn(aTransaction().withFilings(null).build());

            assertThat(transactionService.hasVerdictBeenReached(TRANSACTION_ID, PASSTHROUGH_HEADER)).isFalse();
        }
    }

    @Nested
    @DisplayName("GET /transactions/{transaction_id}/payment")
    class GetTransactionPaymentDetails {

        @BeforeEach
        void initialize() throws IOException {
            when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);
            when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
            when(transactionsResourceHandler.getPayment(PAYMENT_URI)).thenReturn(transactionsPaymentGet);
        }

        @Test
        void getPayment_returnsTransactionPaymentData_ifPaymentExists() throws IOException, URIValidationException {
            TransactionPayment transactionPayment = new TransactionPayment();
            transactionPayment.setPaymentReference(PAYMENT_REFERENCE);

            when(transactionsPaymentGet.execute()).thenReturn(apiGetPaymentResponse);
            when(apiGetPaymentResponse.getData()).thenReturn(transactionPayment);

            var response = transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER);
            assertEquals(transactionPayment, response);
        }

        @Test
        void getPayment_throwsServiceException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsPaymentGet.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(404, "Payment Not Found"));
            assertThrows(ServiceException.class, () -> transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER));
        }

        @Test
        void getPayment_throwsServiceException_ifUriValidationExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsPaymentGet.execute()).thenThrow(new URIValidationException("ERROR"));
            assertThrows(ServiceException.class, () -> transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER));
        }

        @Test
        void getPayment_throwsServiceException_ifIOExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsPaymentGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));
            assertThrows(ServiceException.class, () -> transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER));
        }
    }
}
