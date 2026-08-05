package uk.gov.companieshouse.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.exception.BadRequestException;
import uk.gov.companieshouse.exception.InternalServerErrorException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.exception.TransactionNotFoundException;
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
            throw new BadRequestException("Transaction ID missing from request");
        }

        try {
            final var transaction = transactionService.getTransaction(transactionId, passThroughHeader);
            logger.info("Retrieved transaction details for: " + transactionId);
            request.setAttribute(TRANSACTION_KEY, transaction);
            return true;
        } catch (TransactionNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException("Error retrieving transaction data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String getTransactionId(HttpServletRequest request) {
        final Map<String, String> pathVariables = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return pathVariables.get(TRANSACTION_ID_KEY);
    }
}
