package uk.gov.companieshouse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.companieshouse.exception.TransactionNotFoundException;
import uk.gov.companieshouse.exception.TransactionServiceException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String TRANSACTION_ID = "tx-id-123";
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
            Transaction transaction = new Transaction();
            transaction.setId(TRANSACTION_ID);

            when(transactionsGet.execute()).thenReturn(apiGetResponse);
            when(apiGetResponse.getData()).thenReturn(transaction);

            var response = transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER);
            assertEquals(transaction, response);
        }

        @Test
        void getTransaction_throwsNotFoundException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(404, "404 Not Found"));
            assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
        }

        @Test
        void getTransaction_throwsServiceException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(400, "400 Bad Request"));
            assertThrows(TransactionServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
        }

        @Test
        void getTransaction_throwsServiceException_ifUriValidationExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(new URIValidationException("ERROR"));
            assertThrows(TransactionServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
        }

        @Test
        void getTransaction_throwsServiceException_ifIOExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));
            assertThrows(TransactionServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
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
        void getPayment_throwsNotFoundException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsPaymentGet.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(404, "Payment Not Found"));
            assertThrows(TransactionServiceException.class, () -> transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER));
        }

        @Test
        void getPayment_throwsServiceException_ifUriValidationExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsPaymentGet.execute()).thenThrow(new URIValidationException("ERROR"));
            assertThrows(TransactionServiceException.class, () -> transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER));
        }

        @Test
        void getPayment_throwsServiceException_ifIOExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsPaymentGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));
            assertThrows(TransactionServiceException.class, () -> transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER));
        }
    }
}
