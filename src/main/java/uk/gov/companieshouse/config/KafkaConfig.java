package uk.gov.companieshouse.config;

import org.apache.avro.Schema;
import org.apache.avro.io.EncoderFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.client.RestClient;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.producer.ProducerConfigHelper;

/**
 * Configuration class for the kafka queue used to send emails
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.config.acks}")
    private String acks;

    @Value("${kafka.config.retries:10}")
    private int retries;

    @Value("${kafka.config.isRoundRobin}")
    private boolean isRoundRobin;

    @Value("${kafka.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${kafka.schema.uri.email-send}")
    private String emailSchemaUri;

    public KafkaConfig() {
        // blank no-arg constructor
    }

    @Bean
    CHKafkaProducer producer(ProducerConfig producerConfig) {
        return new CHKafkaProducer(producerConfig);
    }

    @Bean
    ProducerConfig producerConfig() {
        ProducerConfig config = new ProducerConfig();
        ProducerConfigHelper.assignBrokerAddresses(config);

        config.setAcks(Acks.valueOf(acks));
        config.setRoundRobinPartitioner(isRoundRobin);
        config.setRetries(retries);

        return config;
    }

    @Bean
    public RestClient restClient(RestTemplate restTemplate) {
        return new RestClient(restTemplate);
    }

    @Bean
    public Schema fetchSchema(RestClient restClient) {
        byte[] bytes = restClient.getSchema(schemaRegistryUrl, emailSchemaUri);
        String schemaJson = new JSONObject(new String(bytes)).getString("schema");

        return new Schema.Parser().parse(schemaJson);
    }

    @Bean
    public EncoderFactory encoderFactory() {
        return EncoderFactory.get();
    }

    /**
     * Create default response error handler for the rest templates.
     *
     * @return A default response error handler
     */
    @Bean
    ResponseErrorHandler responseErrorHandler() {
        return new DefaultResponseErrorHandler();
    }

    /**
     * Create a rest template for service operations.
     *
     * @param handler A default response error handler
     * @return A rest template for the service operation.
     */
    @Bean
    RestTemplate restTemplate(final ResponseErrorHandler handler) {
        final RestTemplate template = new RestTemplateBuilder().build();
        template.setErrorHandler(handler);

        return template;
    }
}