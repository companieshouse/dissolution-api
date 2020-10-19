package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;

public abstract class ApiConfig {

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.keyInternal}")
    private String apiKeyInternal;

    @Value("${api.url}")
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKeyInternal() {
        return apiKeyInternal;
    }

    public void setApiKeyInternal(String apiKeyInternal) {
        this.apiKeyInternal = apiKeyInternal;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
