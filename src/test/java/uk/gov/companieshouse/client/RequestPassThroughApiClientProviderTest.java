package uk.gov.companieshouse.client;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.sdk.ApiClientService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestPassThroughApiClientProviderTest {

    private static final String PASSTHROUGH_HEADER = "passthrough-token";

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private ApiClient apiClient;

    @Mock
    private InternalApiClient internalApiClient;

    @InjectMocks
    private RequestPassThroughApiClientProvider provider;

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class getApiClient {

        @Test
        void resolves_eric_passthrough_token_from_current_request_and_returns_authenticated_client() throws IOException {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);

            ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
            when(requestAttributes.getRequest()).thenReturn(request);

            try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
                mocked.when(RequestContextHolder::currentRequestAttributes).thenReturn(requestAttributes);
                when(apiClientService.getApiClient(PASSTHROUGH_HEADER)).thenReturn(apiClient);

                var result = provider.getApiClient();

                assertThat(result).isEqualTo(apiClient);
                verify(apiClientService, times(1)).getApiClient(PASSTHROUGH_HEADER);
            }
        }

        @Test
        void when_called_outside_of_a_request_then_illegal_state_exception_thrown() {
            try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
                mocked.when(RequestContextHolder::currentRequestAttributes)
                        .thenThrow(new IllegalStateException("No thread-bound request found"));

                assertThatIllegalStateException().isThrownBy(provider::getApiClient);
            }
        }
    }

    @Nested
    @DisplayNameGeneration(ReplaceUnderscores.class)
    class getInternalApiClient {

        @Test
        void resolves_eric_passthrough_token_from_current_request_and_returns_authenticated_client() throws IOException {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("ERIC-Access-Token")).thenReturn(PASSTHROUGH_HEADER);

            ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
            when(requestAttributes.getRequest()).thenReturn(request);

            try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
                mocked.when(RequestContextHolder::currentRequestAttributes).thenReturn(requestAttributes);
                when(apiClientService.getInternalApiClient(PASSTHROUGH_HEADER)).thenReturn(internalApiClient);

                final InternalApiClient result = provider.getInternalApiClient();

                assertThat(result).isEqualTo(internalApiClient);
                verify(apiClientService, times(1)).getInternalApiClient(PASSTHROUGH_HEADER);
            }
        }

        @Test
        void when_called_outside_of_a_request_then_illegal_state_exception_thrown() {
            try (MockedStatic<RequestContextHolder> mocked = mockStatic(RequestContextHolder.class)) {
                mocked.when(RequestContextHolder::currentRequestAttributes)
                        .thenThrow(new IllegalStateException("No thread-bound request found"));

                assertThatIllegalStateException().isThrownBy(provider::getInternalApiClient);
            }
        }
    }
}
