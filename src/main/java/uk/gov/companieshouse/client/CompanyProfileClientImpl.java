package uk.gov.companieshouse.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.exception.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.service.dissolution.validator.CompanyClosableValidator;

import java.io.IOException;

@Service
public class CompanyProfileClientImpl implements CompanyProfileClient {

    private final CompanyClosableValidator validator;
    private final ApiClientService apiClientService;
    private final Logger logger;

    public CompanyProfileClientImpl(ApiClientService apiClientService, Logger logger, CompanyClosableValidator validator) {
        this.apiClientService = apiClientService;
        this.logger = logger;
        this.validator = validator;
    }

    /**
     * Query the company profile service for a given transaction.
     *
     * @param companyNumber the Company Number
     * @param ericPassThroughHeader includes authorisation details
     * @return the company profile if found
     * @throws CompanyProfileServiceException if not found or an error occurred
     * @throws ServiceUnavailableException if public API is unavailable
     */
    @Override
    public CompanyProfileApi getCompanyProfile(final String companyNumber, final String ericPassThroughHeader)
            throws CompanyProfileServiceException {
        try {
            final String uri = "/company/" + companyNumber;
            final CompanyProfileApi companyProfile = apiClientService.getInternalApiClient(ericPassThroughHeader)
                    .company()
                    .get(uri)
                    .execute()
                    .getData();
            logger.info("Retrieved company profile details: " + companyNumber);
            return companyProfile;
        }
        catch (final ApiErrorResponseException e) {
            if (HttpStatus.NOT_FOUND.value() == e.getStatusCode()) {
                throw new CompanyProfileServiceException("Error Retrieving company profile " + companyNumber, e);
            }
            throw new ServiceUnavailableException("The service is down. Try again later");
        }
        catch (final URIValidationException | IOException e) {
            throw new CompanyProfileServiceException("Error Retrieving company profile " + companyNumber, e);
        }
    }
}
