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
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsUpdate;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionPayment;
import uk.gov.companieshouse.client.ApiClientProvider;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.exception.TransactionNotFoundException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.service.transaction.TransactionFiling;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.OPEN;
import static uk.gov.companieshouse.fixtures.FilingTestDataBuilder.aFiling;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;
import static uk.gov.companieshouse.fixtures.TransactionTestDataBuilder.aTransaction;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.SUBMISSION_URI_PATTERN;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String TRANSACTIONS_URL = "/transactions/";
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String PAYMENT_URI = String.format("/transactions/%s/payment", TRANSACTION_ID);

    @Mock
    private ApiClientProvider apiClientProvider;

    @Mock
    private ApiClient apiClient;

    @Mock
    private TransactionsResourceHandler transactionsResourceHandler;

    @Mock
    private TransactionsGet transactionsGet;

    @Mock
    private TransactionsPaymentGet transactionsPaymentGet;

    @Mock
    private TransactionsUpdate transactionsUpdate;

    @Mock
    private ApiResponse<Transaction> apiGetResponse;

    @Mock
    private ApiResponse<TransactionPayment> apiGetPaymentResponse;

    @Mock
    private ApiResponse<Void> apiPatchResponse;

    @InjectMocks
    private TransactionService transactionService;

    @Nested
    @DisplayName("GET /transactions/{transaction_id}")
    class GetTransactionDetails {
        @BeforeEach
        void initialize() throws IOException {
            when(apiClientProvider.getApiClient()).thenReturn(apiClient);
            when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
            when(transactionsResourceHandler.get(TRANSACTIONS_URL + TRANSACTION_ID)).thenReturn(transactionsGet);
        }

        @Test
        void getTransaction_returnsTransactionData_ifTransactionExists() throws IOException, URIValidationException {
            final var transaction = TransactionFixtures.generateTransaction();

            when(transactionsGet.execute()).thenReturn(apiGetResponse);
            when(apiGetResponse.getData()).thenReturn(transaction);

            var response = transactionService.getTransaction(TRANSACTION_ID);
            assertEquals(transaction, response);
        }

        @Test
        void getTransaction_throwsNotFoundException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(404, "404 Not Found"));
            final var exception = assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransaction(TRANSACTION_ID));
            assertThat(exception.getMessage(),
                    is("No transaction found with id " + TRANSACTION_ID));
        }

        @Test
        void getTransaction_throwsServiceException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(400, "400 Bad Request"));
            assertThrows(ServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID));
        }

        @Test
        void getTransaction_throwsServiceException_ifUriValidationExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(new URIValidationException("ERROR"));
            assertThrows(ServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID));
        }

        @Test
        void getTransaction_throwsServiceException_ifIOExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));
            assertThrows(ServiceException.class, () -> transactionService.getTransaction(TRANSACTION_ID));
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class hasVerdictBeenReached {

        @BeforeEach
        void initialize() throws IOException, URIValidationException {
            when(apiClientProvider.getApiClient()).thenReturn(apiClient);
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

            assertThat(transactionService.hasVerdictBeenReached(TRANSACTION_ID)).isEqualTo(expectedVerdict);
        }

        @Test
        void when_no_dissolution_filing_exists_then_false() {
            when(apiGetResponse.getData()).thenReturn(aTransaction()
                    .withNoFilings()
                    .build());

            assertThat(transactionService.hasVerdictBeenReached(TRANSACTION_ID)).isFalse();
        }

        @Test
        void when_filings_is_null_then_false() {
            when(apiGetResponse.getData()).thenReturn(aTransaction().withFilings(null).build());

            assertThat(transactionService.hasVerdictBeenReached(TRANSACTION_ID)).isFalse();
        }
    }

    @Nested
    @DisplayName("GET /transactions/{transaction_id}/payment")
    class GetTransactionPaymentDetails {

        @BeforeEach
        void initialize() throws IOException {
            when(apiClientProvider.getApiClient()).thenReturn(apiClient);
            when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
            when(transactionsResourceHandler.getPayment(PAYMENT_URI)).thenReturn(transactionsPaymentGet);
        }

        @Test
        void getPayment_returnsTransactionPaymentData_ifPaymentExists() throws IOException, URIValidationException {
            TransactionPayment transactionPayment = new TransactionPayment();
            transactionPayment.setPaymentReference(PAYMENT_REFERENCE);

            when(transactionsPaymentGet.execute()).thenReturn(apiGetPaymentResponse);
            when(apiGetPaymentResponse.getData()).thenReturn(transactionPayment);

            var response = transactionService.getPayment(PAYMENT_URI);
            assertEquals(transactionPayment, response);
        }

        @Test
        void getPayment_throwsServiceException_ifApiErrorResponseExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsPaymentGet.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(404, "Payment Not Found"));
            assertThrows(ServiceException.class, () -> transactionService.getPayment(PAYMENT_URI));
        }

        @Test
        void getPayment_throwsServiceException_ifUriValidationExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsPaymentGet.execute()).thenThrow(new URIValidationException("ERROR"));
            assertThrows(ServiceException.class, () -> transactionService.getPayment(PAYMENT_URI));
        }

        @Test
        void getPayment_throwsServiceException_ifIOExceptionOccurs() throws IOException, URIValidationException {
            when(transactionsPaymentGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));
            assertThrows(ServiceException.class, () -> transactionService.getPayment(PAYMENT_URI));
        }
    }

    @Nested
    @DisplayName("PATCH /transactions/{transaction_id}")
    class PatchTransactionData {

        public static final String COMPANY_NUMBER = "12345678";
        private static final String DISSOLUTION_ID = "12345678";
        private static final String TRANSACTION_ID = "tx-id-123";
        private static final String COMPANY_NAME = "Some Company Ltd";
        private static final String SUBMISSION_URI = String.format(SUBMISSION_URI_PATTERN, TRANSACTION_ID, DISSOLUTION_ID);

        @BeforeEach
        void initialize() throws IOException {
            when(apiClientProvider.getInternalApiClient()).thenReturn(apiClient);
            when(apiClient.transactions()).thenReturn(transactionsResourceHandler);
        }

        @Test
        void when_transaction_is_updated_then_company_name_and_filing_resource_is_set_on_transaction() throws IOException, URIValidationException {
            var transaction = aTransaction().withCompanyNumber(COMPANY_NUMBER).withStatus(OPEN).build();
            var filing = new TransactionFiling(DISSOLUTION_ID, FILING_KIND_DS01, COMPANY_NAME);

            when(transactionsResourceHandler.update(TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(transactionsUpdate);
            when(transactionsUpdate.execute()).thenReturn(apiPatchResponse);
            when(apiPatchResponse.getStatusCode()).thenReturn(204);

            transactionService.updateTransaction(transaction, filing);

            assertThat(transaction.getCompanyName()).isEqualTo(COMPANY_NAME);
            assertThat(transaction.getResources()).containsOnlyKeys(SUBMISSION_URI);
            final var resource = transaction.getResources().get(SUBMISSION_URI);
            assertThat(resource.getKind()).isEqualTo(FILING_KIND_DS01);
            assertThat(resource.getLinks()).containsEntry("resource", SUBMISSION_URI);
            assertThat(resource.getLinks()).containsEntry("validation_status", SUBMISSION_URI + "/validation-status");
            assertThat(resource.getLinks()).containsEntry("costs", SUBMISSION_URI + "/costs");
        }

        @Test
        void when_patch_returns_non_204_then_service_exception_is_thrown() throws IOException, URIValidationException {
            var transaction = aTransaction().withCompanyNumber(COMPANY_NUMBER).withStatus(OPEN).build();
            var filing = new TransactionFiling(DISSOLUTION_ID, FILING_KIND_DS01, COMPANY_NAME);

            when(transactionsResourceHandler.update(TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(transactionsUpdate);
            when(transactionsUpdate.execute()).thenReturn(apiPatchResponse);
            when(apiPatchResponse.getStatusCode()).thenReturn(500);

            assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, filing));
        }

        @Test
        void when_patch_returns_404_then_NotFoundException_is_thrown() throws IOException, URIValidationException {
            var transaction = aTransaction().withCompanyNumber(COMPANY_NUMBER).withStatus(OPEN).build();
            var filing = new TransactionFiling(DISSOLUTION_ID, FILING_KIND_DS01, COMPANY_NAME);

            when(transactionsResourceHandler.update(TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(transactionsUpdate);
            when(transactionsUpdate.execute()).thenThrow(TransactionFixtures.generateApiErrorResponseException(404, "404 Not Found"));

            final var exception = assertThrows(TransactionNotFoundException.class, () -> transactionService.updateTransaction(transaction, filing));
            assertThat(exception.getMessage(),
                    is("Failed to update transaction as no transaction was found with id " + TRANSACTION_ID));
        }

        @Test
        void when_io_exception_occurs_then_service_exception_is_thrown() throws IOException, URIValidationException {
            var transaction = aTransaction().withCompanyNumber(COMPANY_NUMBER).withStatus(OPEN).build();
            var filing = new TransactionFiling(DISSOLUTION_ID, FILING_KIND_DS01, COMPANY_NAME);

            when(transactionsResourceHandler.update(TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(transactionsUpdate);
            when(transactionsUpdate.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));

            assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, filing));
        }

        @Test
        void when_uri_validation_exception_occurs_then_service_exception_is_thrown() throws IOException, URIValidationException {
            var transaction = aTransaction().withCompanyNumber(COMPANY_NUMBER).withStatus(OPEN).build();
            var filing = new TransactionFiling(DISSOLUTION_ID, FILING_KIND_DS01, COMPANY_NAME);

            when(transactionsResourceHandler.update(TRANSACTIONS_URL + TRANSACTION_ID, transaction)).thenReturn(transactionsUpdate);
            when(transactionsUpdate.execute()).thenThrow(new URIValidationException("ERROR"));

            assertThrows(ServiceException.class, () -> transactionService.updateTransaction(transaction, filing));
        }
    }
}
