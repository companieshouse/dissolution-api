package uk.gov.companieshouse.service.transaction;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.client.ApiClientProvider;
import uk.gov.companieshouse.exception.ServiceException;

import java.io.IOException;

@Service
public class TransactionPaymentService {

    private final ApiClientProvider apiClientProvider;

    public TransactionPaymentService(ApiClientProvider apiClientProvider) {
        this.apiClientProvider = apiClientProvider;
    }

    public PaymentApi getPaymentSession(String paymentReference) throws ServiceException {
        try {
            return apiClientProvider.getApiClient()
                    .payment()
                    .get("/payments/" + paymentReference)
                    .execute()
                    .getData();
        } catch (URIValidationException | IOException e) {
            throw new ServiceException("Failed to retrieve payment session data for: " + paymentReference, e);
        }
    }
}
