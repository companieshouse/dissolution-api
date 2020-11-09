package uk.gov.companieshouse.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.gov.companieshouse.api.interceptor.InternalUserInterceptor;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.exception.UnauthorisedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TokenOrApiKeyPermissionsInterceptor extends HandlerInterceptorAdapter {

    public static final String OAUTH2_IDENTITY_TYPE = "oauth2";
    public static final String API_KEY_IDENTITY_TYPE = "key";

    private final DissolutionTokenPermissionsInterceptor dissolutionTokenPermissionsInterceptor;
    private final InternalUserInterceptor apiKeyPermissionsInterceptor;

    @Autowired
    public TokenOrApiKeyPermissionsInterceptor(
        DissolutionTokenPermissionsInterceptor dissolutionTokenPermissionsInterceptor,
        InternalUserInterceptor apiKeyPermissionsInterceptor
    ) {
        this.dissolutionTokenPermissionsInterceptor = dissolutionTokenPermissionsInterceptor;
        this.apiKeyPermissionsInterceptor = apiKeyPermissionsInterceptor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        final String identityType = request.getHeader(EricConstants.ERIC_IDENTITY_TYPE);

        if (identityType.equals(OAUTH2_IDENTITY_TYPE)) {
            return dissolutionTokenPermissionsInterceptor.preHandle(request, response, null);
        }

        if (identityType.equals(API_KEY_IDENTITY_TYPE)) {
            return apiKeyPermissionsInterceptor.preHandle(request, response, null);
        }

        throw new UnauthorisedException("No authorised identity in request");
    }
}
