package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.interceptor.DissolutionTokenPermissionsInterceptor;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private static final String URI_PATTERN = "/dissolution-request/**";

    private static final String PAYMENT_ENDPOINT = "/dissolution-request/{company-number}/payment";
    private static final String SUBMIT_ENDPOINT = "/dissolution-request/submit";

    private static final String[] TOKEN_PERMISSION_AUTH_EXCLUDE_LIST = {
            "/dissolution-request/healthcheck",
            PAYMENT_ENDPOINT,
            SUBMIT_ENDPOINT
    };

    private static final String[] API_KEY_PERMISSION_AUTH_INCLUDE_LIST = {
            PAYMENT_ENDPOINT,
            SUBMIT_ENDPOINT
    };

    @Autowired
    private TokenPermissionsInterceptor tokenPermissionsInterceptor;

    @Autowired
    private DissolutionTokenPermissionsInterceptor dissolutionTokenPermissionsInterceptor;

    @Autowired
    private InternalUserInterceptor apiKeyPermissionsInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(tokenPermissionsInterceptor).addPathPatterns(URI_PATTERN).excludePathPatterns(TOKEN_PERMISSION_AUTH_EXCLUDE_LIST);
        registry.addInterceptor(dissolutionTokenPermissionsInterceptor).addPathPatterns(URI_PATTERN).excludePathPatterns(TOKEN_PERMISSION_AUTH_EXCLUDE_LIST);
        registry.addInterceptor(apiKeyPermissionsInterceptor).addPathPatterns(API_KEY_PERMISSION_AUTH_INCLUDE_LIST);
    }
}
