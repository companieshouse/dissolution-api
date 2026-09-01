package uk.gov.companieshouse.client;

import uk.gov.companieshouse.api.ApiClient;

import java.io.IOException;

/**
 * Supplies an {@link ApiClient} already authenticated for the current request, so that callers
 * don't need to know about, or thread through, the Eric pass-through token header themselves.
 */
public interface ApiClientProvider {

    ApiClient getApiClient() throws IOException;
    ApiClient getInternalApiClient() throws IOException;
}
