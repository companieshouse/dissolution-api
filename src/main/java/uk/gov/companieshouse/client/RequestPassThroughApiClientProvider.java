package uk.gov.companieshouse.client;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.sdk.ApiClientService;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

import java.io.IOException;

/**
 * Default {@link ApiClientProvider} that resolves the Eric pass-through token header from the
 * current request and uses it to build an authenticated {@link ApiClient}.
 *
 * <p>Only usable while a request is being handled on the calling thread (e.g. not from a
 * scheduled job, async method, or message listener); calling it outside of that context throws
 * {@link IllegalStateException} rather than silently resolving a {@code null} token.
 */
@Component
public class RequestPassThroughApiClientProvider implements ApiClientProvider {

    private final ApiClientService apiClientService;

    public RequestPassThroughApiClientProvider(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    public ApiClient getApiClient() throws IOException {
        return apiClientService.getApiClient(getPassThroughTokenHeader());
    }

    @Override
    public ApiClient getInternalApiClient() throws IOException {
        return apiClientService.getInternalApiClient(getPassThroughTokenHeader());
    }

    private String getPassThroughTokenHeader() {
        var attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attributes.getRequest().getHeader(ApiSdkManager.getEricPassthroughTokenHeader());
    }
}
