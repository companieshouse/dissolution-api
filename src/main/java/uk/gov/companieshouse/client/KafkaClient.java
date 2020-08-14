package uk.gov.companieshouse.client;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KafkaClient {

    public String getSchema(String schemaRegistryUrl, String emailSchemaUri) {
        return WebClient
                .create(schemaRegistryUrl)
                .get()
                .uri(emailSchemaUri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
