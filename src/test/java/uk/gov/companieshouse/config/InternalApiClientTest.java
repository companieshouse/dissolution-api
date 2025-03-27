package uk.gov.companieshouse.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;

import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.companieshouse.model.Constants.EMAIL_APP_ID;

@ExtendWith(MockitoExtension.class)
class InternalApiClientTest {

    private InternalApiConfig internalApiConfig;

    @BeforeEach
    void setup() {
        internalApiConfig = new InternalApiConfig("test-api-key", "http://chs-kafka-api-url:8080");
    }

    @Test
    void testCreateInternalApiClient() {
        InternalApiClient internalApiClient = internalApiConfig.kafkaApiClientSupplier();

        assertThat(internalApiClient.getBasePath(), is("http://chs-kafka-api-url:8080"));
        assertThat(internalApiClient.getHttpClient().getRequestId(), is(EMAIL_APP_ID));
    }

    @Test
    void testConcurrentMap() {
        ConcurrentMap<Object, Object> concurrentMap = internalApiConfig.concurrentMap();

        assertThat(concurrentMap.size(), is(0));
    }
}
