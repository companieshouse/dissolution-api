package uk.gov.companieshouse.service;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.exception.CompanyProfileServiceException;
import uk.gov.companieshouse.exception.ServiceUnavailableException;

public interface CompanyProfileServiceA {

    /**
     * Query the company profile service for a given transaction.
     *
     * @param companyNumber the Company Number
     * @param ericPassThroughHeader includes authorisation details
     * @return the company profile if found
     * @throws CompanyProfileServiceException if not found or an error occurred
     * @throws ServiceUnavailableException if public API is unavailable
     */
    CompanyProfileApi getCompanyProfile(final String companyNumber, final String ericPassThroughHeader)
            throws CompanyProfileServiceException, ServiceUnavailableException;
}
