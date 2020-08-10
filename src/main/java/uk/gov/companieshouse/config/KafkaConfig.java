package uk.gov.companieshouse.config;

import org.apache.avro.Schema;
import org.apache.avro.io.EncoderFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.client.KafkaClient;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;

/**
 * Configuration class for the kafka queue used to send emails
 */
@Configuration
public class KafkaConfig {

    @Value("${kafka.broker.addr}")
    private String brokerAddr;

    @Value("${kafka.config.acks}")
    private String acks;

    @Value("${kafka.config.retries}")
    private int retries;

    @Value("${kafka.config.isRoundRobin}")
    private boolean isRoundRobin;

    @Value("${kafka.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${kafka.schema.uri.email-send}")
    private String emailSchemaUri;

    @Bean
    CHKafkaProducer producer(ProducerConfig producerConfig) {
        return new CHKafkaProducer(producerConfig);
    }

    @Bean
    ProducerConfig producerConfig() {
        ProducerConfig config = new ProducerConfig();
        config.setBrokerAddresses(brokerAddr.split(","));
        config.setAcks(Acks.valueOf(acks));
        config.setRoundRobinPartitioner(isRoundRobin);
        config.setRetries(retries);

        return config;
    }

    @Bean
    public Schema fetchSchema(KafkaClient kafkaClient) {
        String schema = kafkaClient.getSchema(schemaRegistryUrl, emailSchemaUri);
        String schemaJson = new JSONObject(schema).getString("schema");

        return new Schema.Parser().parse(schemaJson);
    }

    @Bean
    public EncoderFactory encoderFactory() {
        return EncoderFactory.get();
    }
}