package uk.gov.companieshouse.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.exception.ServiceException;

import java.io.IOException;

@Service
public class TransactionService {

    private final ApiClientService apiClientService;

    public TransactionService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    public Transaction getTransaction(String transactionId, String passthroughHeader) throws ServiceException {
        try {
            final var uri = "/transactions/" + transactionId;
            return apiClientService.getApiClient(passthroughHeader)
                    .transactions()
                    .get(uri)
                    .execute()
                    .getData();
        } catch (URIValidationException | IOException e) {
            throw new ServiceException("Failed to retrieve transaction details for: " + transactionId, e);
        }
    }
}
