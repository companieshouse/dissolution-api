package uk.gov.companieshouse.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionPayment;
import uk.gov.companieshouse.client.ApiClientProvider;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.exception.TransactionNotFoundException;
import uk.gov.companieshouse.model.Constants;

import java.io.IOException;

@Service
public class TransactionService {

    private final ApiClientProvider apiClientProvider;

    public TransactionService(ApiClientProvider apiClientProvider) {
        this.apiClientProvider = apiClientProvider;
    }

    public Transaction getTransaction(String transactionId) {
        final var uri = "/transactions/" + transactionId;

        try {
            return apiClientProvider.getApiClient()
                    .transactions()
                    .get(uri)
                    .execute()
                    .getData();
        } catch (ApiErrorResponseException e) {
            final var status = e.getStatusCode();
            if (status == HttpStatus.NOT_FOUND.value()) {
                throw new TransactionNotFoundException(String.format("No transaction found with id %s", transactionId));
            }
            throw new ServiceException(String.format("Failed to retrieve transaction details for %s, received http status code %s", transactionId, status), e);
        } catch (URIValidationException | IOException e) {
            throw new ServiceException(String.format("Failed to retrieve transaction details for: %s", transactionId), e);
        }
    }

    public boolean hasVerdictBeenReached(String transactionId) {
        final Transaction transaction = getTransaction(transactionId);

        if (transaction.getFilings() == null) {
            return false;
        }

        return transaction.getFilings().values().stream()
                .filter(filing -> filing.getType() != null && filing.getType().startsWith(Constants.FILING_TYPE_PREFIX_DISSOLUTION))
                .anyMatch(filing -> FilingStatus.ACCEPTED.matches(filing.getStatus()) || FilingStatus.REJECTED.matches(filing.getStatus()));
    }

    public TransactionPayment getPayment(String uri) {
        try {
            return apiClientProvider.getApiClient()
                    .transactions()
                    .getPayment(uri)
                    .execute()
                    .getData();
        } catch (URIValidationException | IOException e) {
            throw new ServiceException(String.format("Failed to retrieve transaction payment details for: %s", uri), e);
        }
    }
}
