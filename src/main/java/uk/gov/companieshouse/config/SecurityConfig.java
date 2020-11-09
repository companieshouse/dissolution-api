package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.interceptor.TokenOrApiKeyPermissionsInterceptor;
import uk.gov.companieshouse.interceptor.DissolutionTokenPermissionsInterceptor;

import java.util.Arrays;
import java.util.stream.Stream;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private static final String URI_PATTERN = "/dissolution-request/**";

    private static final String[] API_KEY_PERMISSION_AUTH_INCLUDE_LIST = {
        "/dissolution-request/submit",
        "/dissolution-request/response"
    };

    private static final String[] TOKEN_OR_API_KEY_PERMISSION_AUTH_INCLUDE_LIST = {
        "/dissolution-request/{application-reference}/payment"
    };

    private static final String[] TOKEN_PERMISSION_AUTH_EXCLUDE_LIST =
        Stream.of(
                Arrays.stream(API_KEY_PERMISSION_AUTH_INCLUDE_LIST),
                Arrays.stream(TOKEN_OR_API_KEY_PERMISSION_AUTH_INCLUDE_LIST),
                "/dissolution-request/healthcheck"
        ).toArray(String[]::new);

    @Autowired
    private TokenPermissionsInterceptor tokenPermissionsInterceptor;

    @Autowired
    private DissolutionTokenPermissionsInterceptor dissolutionTokenPermissionsInterceptor;

    @Autowired
    private InternalUserInterceptor apiKeyPermissionsInterceptor;

    @Autowired
    private TokenOrApiKeyPermissionsInterceptor tokenOrApiKeyPermissionsInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(tokenPermissionsInterceptor).addPathPatterns(URI_PATTERN).excludePathPatterns(TOKEN_PERMISSION_AUTH_EXCLUDE_LIST);
        registry.addInterceptor(dissolutionTokenPermissionsInterceptor).addPathPatterns(URI_PATTERN).excludePathPatterns(TOKEN_PERMISSION_AUTH_EXCLUDE_LIST);
        registry.addInterceptor(apiKeyPermissionsInterceptor).addPathPatterns(API_KEY_PERMISSION_AUTH_INCLUDE_LIST);
        registry.addInterceptor(tokenOrApiKeyPermissionsInterceptor).addPathPatterns(TOKEN_OR_API_KEY_PERMISSION_AUTH_INCLUDE_LIST);
    }
}
