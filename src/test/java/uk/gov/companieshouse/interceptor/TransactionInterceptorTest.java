package uk.gov.companieshouse.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.exception.UnauthorisedException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.service.TransactionService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_KEY;

@ExtendWith(MockitoExtension.class)
class TransactionInterceptorTest {

    private static final String TX_ID = "12345678";
    private static final String PASSTHROUGH_HEADER = "passthrough";

    @Mock
    private TransactionService transactionService;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private Logger logger;

    @InjectMocks
    private TransactionInterceptor transactionInterceptor;

    @Test
    void interceptor_returnsTrue_ifTransactionExists() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();
        Transaction dummyTransaction = new Transaction();
        dummyTransaction.setId(TX_ID);

        var pathParams = new HashMap<String, String>();
        pathParams.put(TRANSACTION_ID_KEY, TX_ID);

        when(transactionService.getTransaction(TX_ID, PASSTHROUGH_HEADER)).thenReturn(dummyTransaction);
        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);

        assertTrue(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        verify(mockHttpServletRequest, times(1)).setAttribute(TRANSACTION_KEY, dummyTransaction);
    }

    @Test
    void interceptor_returnsFalse_ifServiceExceptionOccurs() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        var pathParams = new HashMap<String, String>();
        pathParams.put(TRANSACTION_ID_KEY, TX_ID);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);
        when(transactionService.getTransaction(TX_ID, PASSTHROUGH_HEADER)).thenThrow(ServiceException.class);

        assertFalse(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, mockHttpServletResponse.getStatus());
    }

    @Test
    void interceptor_throwsAnUnauthorisedError_ifTransactionIdIsMissing() {
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);
        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(Map.of());

        assertFalse(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, mockHttpServletResponse.getStatus());
        verify(transactionService, never()).getTransaction(TX_ID, PASSTHROUGH_HEADER);
    }

    @Test
    void interceptor_throwsAnUnAuthorisedError_whenTransactionLookUpIsUnauthorised(){
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        Object mockHandler = new Object();

        var pathParams = new HashMap<String, String>();
        pathParams.put(TRANSACTION_ID_KEY, TX_ID);

        when(mockHttpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(pathParams);
        when(mockHttpServletRequest.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);
        when(transactionService.getTransaction(TX_ID, PASSTHROUGH_HEADER)).thenThrow(UnauthorisedException.class);

        assertFalse(transactionInterceptor.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler));
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, mockHttpServletResponse.getStatus());
    }
}
