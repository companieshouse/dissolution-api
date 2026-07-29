package uk.gov.companieshouse.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.exception.UnauthorisedException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;
import uk.gov.companieshouse.service.TransactionService;

import java.util.Map;

import static uk.gov.companieshouse.model.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_KEY;

@Component
public class TransactionInterceptor implements HandlerInterceptor {

    private final TransactionService transactionService;
    private final Logger logger;

    public TransactionInterceptor(TransactionService transactionService, Logger logger) {
        this.transactionService = transactionService;
        this.logger = logger;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        final var passThroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());
        final var transactionId = getTransactionId(request);

        if (StringUtils.isBlank(transactionId)) {
            logger.info("Transaction ID missing from request");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        try {
            final var transaction = transactionService.getTransaction(transactionId, passThroughHeader);
            logger.info("Retrieved transaction details for: " + transactionId);
            request.setAttribute(TRANSACTION_KEY, transaction);
            return true;
        } catch (Exception e) {
            logger.error("Failed to retrieve transaction details for: " + transactionId, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private String getTransactionId(HttpServletRequest request) {
        final Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return pathVariables.get(TRANSACTION_ID_KEY);
    }
}
