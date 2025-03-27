package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;

import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.companieshouse.model.Constants.EMAIL_APP_ID;

@Configuration
public class InternalApiConfig {

    private final String apiKey;
    private final String kafkaApiUrl;

    public InternalApiConfig(@Value("${api.key}") String apiKey, @Value("${kafka.api.url}") String kafkaApiUrl) {
        this.apiKey = apiKey;
        this.kafkaApiUrl = kafkaApiUrl;
    }

    @Bean
    public InternalApiClient kafkaApiClientSupplier() {
        var apiKeyHttpClient = new ApiKeyHttpClient(apiKey);
        apiKeyHttpClient.setRequestId(EMAIL_APP_ID);

        var internalApiClient = new InternalApiClient(apiKeyHttpClient);
        internalApiClient.setBasePath(kafkaApiUrl);

        return internalApiClient;
    }

    @Bean
    public <T> ConcurrentHashMap<T, T> concurrentHashMap() {
        return new ConcurrentHashMap<>();
    }
}
