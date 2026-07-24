package uk.gov.companieshouse.config;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.interceptor.TokenPermissionsInterceptor;
import uk.gov.companieshouse.interceptor.DissolutionTokenPermissionsInterceptor;
import uk.gov.companieshouse.interceptor.TransactionInterceptor;

@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    private static final String URI_PATTERN = "/dissolution-request/**";
    private static final String COSTS_URI = "/transactions/{transaction_id}/dissolution/{dissolution_id}/costs";

    private static final String[] API_KEY_PERMISSION_AUTH_INCLUDE_LIST = {
            "/dissolution-request/{application-reference}/payment",
            "/dissolution-request/submit",
            "/dissolution-request/response",
            "/dissolution-request/{company-number}/resend-email/{email-address}"
    };

    private static final String[] TOKEN_PERMISSION_AUTH_EXCLUDE_LIST = ArrayUtils.addAll(
        API_KEY_PERMISSION_AUTH_INCLUDE_LIST,
        "/dissolution-api/healthcheck"
    );

    private static final String[] TRANSACTIONS_INCLUDE_LIST = {
            "/transactions/**",
            "/private/transactions/**",
    };

    private final TokenPermissionsInterceptor tokenPermissionsInterceptor;
    private final DissolutionTokenPermissionsInterceptor dissolutionTokenPermissionsInterceptor;
    private final InternalUserInterceptor apiKeyPermissionsInterceptor;
    private final TransactionInterceptor transactionInterceptor;

    public SecurityConfig(
            TokenPermissionsInterceptor tokenPermissionsInterceptor,
            DissolutionTokenPermissionsInterceptor dissolutionTokenPermissionsInterceptor,
            InternalUserInterceptor apiKeyPermissionsInterceptor,
            TransactionInterceptor transactionInterceptor) {
        this.tokenPermissionsInterceptor = tokenPermissionsInterceptor;
        this.dissolutionTokenPermissionsInterceptor = dissolutionTokenPermissionsInterceptor;
        this.apiKeyPermissionsInterceptor = apiKeyPermissionsInterceptor;
        this.transactionInterceptor = transactionInterceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(tokenPermissionsInterceptor).addPathPatterns(URI_PATTERN).excludePathPatterns(TOKEN_PERMISSION_AUTH_EXCLUDE_LIST);
        registry.addInterceptor(dissolutionTokenPermissionsInterceptor).addPathPatterns(URI_PATTERN).excludePathPatterns(TOKEN_PERMISSION_AUTH_EXCLUDE_LIST);
        registry.addInterceptor(apiKeyPermissionsInterceptor).addPathPatterns(API_KEY_PERMISSION_AUTH_INCLUDE_LIST);
        registry.addInterceptor(transactionInterceptor).addPathPatterns(TRANSACTIONS_INCLUDE_LIST);
    }
}
