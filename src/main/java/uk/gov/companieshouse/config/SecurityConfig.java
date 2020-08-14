package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.interceptor.DissolutionTokenPermissionsInterceptor;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private static final String URI_PATTERN = "/dissolution-request/*";

    private static final String[] TOKEN_PERMISSION_AUTH_WHITELIST = {
            "/dissolution-request/healthcheck",
            "/dissolution-request/{company-number}/payment",
            "/dissolution-request/submit"
    };

    @Autowired
    private TokenPermissionsInterceptor tokenPermissionsInterceptor;

    @Autowired
    private DissolutionTokenPermissionsInterceptor dissolutionTokenPermissionsInterceptor;

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(tokenPermissionsInterceptor).addPathPatterns(URI_PATTERN).excludePathPatterns(TOKEN_PERMISSION_AUTH_WHITELIST);
        registry.addInterceptor(dissolutionTokenPermissionsInterceptor).addPathPatterns(URI_PATTERN).excludePathPatterns(TOKEN_PERMISSION_AUTH_WHITELIST);
    }

}
