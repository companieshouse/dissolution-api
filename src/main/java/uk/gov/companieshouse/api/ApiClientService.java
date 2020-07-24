package uk.gov.companieshouse.api;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.sdk.manager.ApiClientManager;

@Component
public class ApiClientService {
    public ApiClient getPublicApiClient() {
        return ApiClientManager.getSDK();
    }
}