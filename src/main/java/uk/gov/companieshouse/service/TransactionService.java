package uk.gov.companieshouse.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionPayment;
import uk.gov.companieshouse.client.ApiClientProvider;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.exception.TransactionNotFoundException;
import uk.gov.companieshouse.model.Constants;
import uk.gov.companieshouse.service.transaction.TransactionFiling;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.model.Constants.SUBMISSION_URI_PATTERN;

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

    public void updateTransaction(Transaction transaction, final TransactionFiling filing) {
        final var submissionUri = String.format(SUBMISSION_URI_PATTERN, transaction.getId(), filing.id());
        final var submissionLinks = createResourceLinks(submissionUri);

        transaction.setCompanyName(filing.companyName());
        transaction.setResources(Collections.singletonMap(submissionUri, createTransactionResource(filing.kind(), submissionLinks)));
        patchTransaction(transaction);
    }

    private Resource createTransactionResource(String kind, Map<String, String> links) {
        final var resource = new Resource();
        resource.setKind(kind);
        resource.setLinks(links);
        return resource;
    }

    private Map<String, String> createResourceLinks(String uri) {
        Map<String, String> linksMap = new HashMap<>();
        linksMap.put("resource", uri);
        linksMap.put("validation_status", uri + "/validation-status");
        linksMap.put("costs", uri + "/costs");
        return linksMap;
    }

    private void patchTransaction(Transaction transaction) throws ServiceException {
        final var uri = "/transactions/" + transaction.getId();
        try {
            var resp = apiClientProvider.getInternalApiClient()
                    .transactions()
                    .update(uri, transaction)
                    .execute();

            if (resp.getStatusCode() != 204) {
                throw new IOException(String.format("Received invalid http status code %s for transaction %s", resp.getStatusCode(), transaction.getId()));
            }
        } catch (ApiErrorResponseException e) {
            final var status = e.getStatusCode();
            if (status == HttpStatus.NOT_FOUND.value()) {
                throw new TransactionNotFoundException(String.format("Failed to update transaction as no transaction was found with id %s", transaction.getId()));
            }
            throw new ServiceException(String.format("Failed to update transaction for %s, received http status code %s", transaction.getId(), status), e);
        } catch (IOException | URIValidationException e) {
            throw new ServiceException("Failed to update transaction " + transaction.getId(), e);
        }
    }
}
