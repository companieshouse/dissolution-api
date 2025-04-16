package uk.gov.companieshouse.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(MockitoExtension.class)
class InternalApiClientTest {

    private Supplier<InternalApiClient> internalApiClientSupplier;

    @BeforeEach
    void setup() {
        internalApiClientSupplier = new InternalApiConfig().internalApiClientSupplier(
                "test-api-key", "http://chs-kafka-api-url:8080");
    }

    @Test
    void testCreateInternalApiClient() {
        InternalApiClient internalApiClient = internalApiClientSupplier.get();

        assertThat(internalApiClient.getBasePath(), is("http://chs-kafka-api-url:8080"));
        assertThat(internalApiClient.getHttpClient().getRequestId(), is(nullValue()));
    }

    @Test
    void testConcurrentMap() {
        ConcurrentMap<Object, Object> concurrentMap = new InternalApiConfig().concurrentMap();

        assertThat(concurrentMap.size(), is(0));
    }
}
