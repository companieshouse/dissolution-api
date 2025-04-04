package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Configuration
public class InternalApiConfig {

    @Bean
    public Supplier<InternalApiClient> internalApiClientSupplier(@Value("${api.key}") String apiKey, @Value("${api.url}") String apiUrl) {
        var internalApiClient = new InternalApiClient(new ApiKeyHttpClient(apiKey));
        internalApiClient.setBasePath(apiUrl);

        return () -> internalApiClient;
    }

    @Bean
    public <T> ConcurrentMap<T, T> concurrentMap() {
        return new ConcurrentHashMap<>();
    }
}
