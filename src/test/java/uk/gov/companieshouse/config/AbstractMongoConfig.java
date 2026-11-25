package uk.gov.companieshouse.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class AbstractMongoConfig {

    // Shared with AbstractIntegrationTest so repository tests and integration tests run against the same server.
    public static final String MONGO_IMAGE = "mongo:8.2.5-noble";

    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(
            DockerImageName.parse(MONGO_IMAGE));

    @DynamicPropertySource
    public static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        mongoDBContainer.start();
    }
}
