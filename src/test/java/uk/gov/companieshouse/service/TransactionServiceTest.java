package uk.gov.companieshouse.service;

import com.google.api.client.http.HttpResponseException;
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
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.exception.TransactionNotFoundException;
import uk.gov.companieshouse.exception.TransactionServiceException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String TRANSACTION_ID = "12345678";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String TRANSACTIONS_URL = "/transactions/";

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private ApiClient apiClient;

    @Mock
    private TransactionsResourceHandler transactionsResourceHandler;

    @Mock
    private TransactionsGet transactionsGet;

    @Mock
    private ApiResponse<Transaction> apiGetResponse;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void getTransaction_returnsTransactionData_ifTransactionExists() throws IOException, URIValidationException {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);

        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(transactionsResourceHandler.get(TRANSACTIONS_URL + TRANSACTION_ID)).thenReturn(transactionsGet);
        when(transactionsGet.execute()).thenReturn(apiGetResponse);
        when(apiGetResponse.getData()).thenReturn(transaction);

        var response = transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER);

        assertEquals(transaction, response);
    }

    @Test
    void getTransaction_throwsNotFoundException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(new HttpResponseException.Builder(404, "404 Not Found", new com.google.api.client.http.HttpHeaders()));
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(transactionsResourceHandler.get(TRANSACTIONS_URL + TRANSACTION_ID)).thenReturn(transactionsGet);
        when(transactionsGet.execute()).thenThrow(apiErrorResponseException);

        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
    }

    @Test
    void getTransaction_throwsServiceException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(new HttpResponseException.Builder(400, "400 Bad Request", new com.google.api.client.http.HttpHeaders()));
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(transactionsResourceHandler.get(TRANSACTIONS_URL + TRANSACTION_ID)).thenReturn(transactionsGet);
        when(transactionsGet.execute()).thenThrow(apiErrorResponseException);

        assertThrows(TransactionServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
    }

    @Test
    void getTransaction_throwsServiceException_ifUriValidationExceptionOccurs() throws IOException, URIValidationException {
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(transactionsResourceHandler.get(TRANSACTIONS_URL + TRANSACTION_ID)).thenReturn(transactionsGet);
        when(transactionsGet.execute()).thenThrow(new URIValidationException("ERROR"));

        assertThrows(TransactionServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
    }

    @Test
    void getTransaction_throwsServiceException_ifIOExceptionOccurs() throws IOException, URIValidationException {
        when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);
        when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        when(transactionsResourceHandler.get(TRANSACTIONS_URL + TRANSACTION_ID)).thenReturn(transactionsGet);
        when(transactionsGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));

        assertThrows(TransactionServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER));
    }
}
