package uk.gov.companieshouse.client;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.exception.ServiceUnavailableException;

import java.util.Optional;

public interface CompanyProfileClient {

    /**
     * Query the company profile service for a given transaction.
     *
     * @param companyNumber the Company Number
     * @param ericPassThroughHeader includes authorisation details
     * @return an Optional containing the company profile, or empty if the company is not found
     * @throws CompanyProfileServiceException if an error occurred
     * @throws ServiceUnavailableException if public API is unavailable
     */
    Optional<CompanyProfileApi> getCompanyProfile(final String companyNumber, final String ericPassThroughHeader)
            throws CompanyProfileServiceException, ServiceUnavailableException;
}
