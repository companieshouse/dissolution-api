package uk.gov.companieshouse.service.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.payment.PaymentResourceHandler;
import uk.gov.companieshouse.api.handler.payment.request.PaymentGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.client.ApiClientProvider;
import uk.gov.companieshouse.exception.ServiceException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionPaymentServiceTest {

    private static final String PAYMENT_METHOD = "credit-card";
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String PAYMENT_URI = String.format("/payments/%s", PAYMENT_REFERENCE);

    @Mock
    private ApiClientProvider apiClientProvider;

    @Mock
    private ApiClient apiClient;

    @Mock
    private PaymentResourceHandler paymentResourceHandler;

    @Mock
    private PaymentGet paymentGet;

    @Mock
    private ApiResponse<PaymentApi> apiGetResponse;

    @InjectMocks
    private TransactionPaymentService service;

    @BeforeEach
    void initialize() throws IOException {
        when(apiClientProvider.getApiClient()).thenReturn(apiClient);
        when(apiClient.payment()).thenReturn(paymentResourceHandler);
        when(paymentResourceHandler.get(PAYMENT_URI)).thenReturn(paymentGet);
    }

    @Test
    void getPaymentSession_returnsTransactionData_ifTransactionExists() throws IOException, URIValidationException {
        PaymentApi paymentSession = new PaymentApi();
        paymentSession.setPaymentMethod(PAYMENT_METHOD);

        when(paymentGet.execute()).thenReturn(apiGetResponse);
        when(apiGetResponse.getData()).thenReturn(paymentSession);

        var response = service.getPaymentSession(PAYMENT_REFERENCE);
        assertEquals(paymentSession, response);
    }

    @Test
    void getPaymentSession_throwsServiceException_ifUriValidationExceptionOccurs() throws IOException, URIValidationException {
        when(paymentGet.execute()).thenThrow(new URIValidationException("ERROR"));
        assertThrows(ServiceException.class, () -> service.getPaymentSession(PAYMENT_REFERENCE));
    }

    @Test
    void getPaymentSession_throwsServiceException_ifIOExceptionOccurs() throws IOException, URIValidationException {
        when(paymentGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("ERROR")));
        assertThrows(ServiceException.class, () -> service.getPaymentSession(PAYMENT_REFERENCE));
    }
}
