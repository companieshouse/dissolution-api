package uk.gov.companieshouse.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class OpenTelemetryAppenderInitializerTest {

    private OpenTelemetry openTelemetry;
    private OpenTelemetryAppenderInitializer initializer;

    @BeforeEach
    void setUp() {
        openTelemetry = mock(OpenTelemetry.class);
        initializer = new OpenTelemetryAppenderInitializer(openTelemetry);
    }

    @Test
    void afterPropertiesSet_installsAppenderWithoutException() {
        try (MockedStatic<OpenTelemetryAppender> appenderMock = org.mockito.Mockito.mockStatic(OpenTelemetryAppender.class)) {
            assertDoesNotThrow(() -> initializer.afterPropertiesSet());
            appenderMock.verify(() -> OpenTelemetryAppender.install(openTelemetry));
        }
    }

    @Test
    void constructor_acceptsNullOpenTelemetry() {
        OpenTelemetryAppenderInitializer nullInitializer = new OpenTelemetryAppenderInitializer(null);
        try (MockedStatic<OpenTelemetryAppender> appenderMock = org.mockito.Mockito.mockStatic(OpenTelemetryAppender.class)) {
            assertDoesNotThrow(nullInitializer::afterPropertiesSet);
            appenderMock.verify(() -> OpenTelemetryAppender.install(null));
        }
    }
}

