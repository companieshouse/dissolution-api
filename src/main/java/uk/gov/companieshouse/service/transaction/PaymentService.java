package uk.gov.companieshouse.service.transaction;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.exception.ServiceException;

import java.io.IOException;

@Service
public class PaymentService {

    private final ApiClientService apiClientService;

    public PaymentService(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    public PaymentApi getPaymentSession(String paymentReference, String passThroughTokenHeader) throws ServiceException {
        try {
            return apiClientService.getApiClient(passThroughTokenHeader)
                    .payment()
                    .get("/payments/" + paymentReference)
                    .execute()
                    .getData();
        } catch (URIValidationException | IOException e) {
            throw new ServiceException("Failed to retrieve payment session data for: " + paymentReference, e);
        }
    }
}
