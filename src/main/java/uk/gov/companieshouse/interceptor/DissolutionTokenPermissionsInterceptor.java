package uk.gov.companieshouse.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import uk.gov.companieshouse.api.util.security.AuthorisationUtil;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.api.util.security.TokenPermissions;
import uk.gov.companieshouse.exception.UnauthorisedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class DissolutionTokenPermissionsInterceptor extends HandlerInterceptorAdapter {

    private static final String PATH_VARIABLE_COMPANY_NUMBER = "company-number";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final TokenPermissions tokenPermissions = AuthorisationUtil
                .getTokenPermissions(request)
                .orElseThrow(() -> new UnauthorisedException("TokenPermissions not present in request"));

        if (!isAuthorisedForCompany(request, tokenPermissions)) {
            throw new UnauthorisedException("User is not authorised for company");
        }

        if (!isAuthorisedToPerformDissolution(tokenPermissions)) {
            throw new UnauthorisedException("User is not authorised to perform dissolution");
        }

        return true;
    }

    private boolean isAuthorisedForCompany(HttpServletRequest request, TokenPermissions tokenPermissions) {
        final String companyNumber = getCompanyNumber(request);
        return tokenPermissions.hasPermission(Permission.Key.COMPANY_NUMBER, companyNumber);
    }

    private String getCompanyNumber(HttpServletRequest request) {
        return ((Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .get(PATH_VARIABLE_COMPANY_NUMBER);
    }

    private boolean isAuthorisedToPerformDissolution(TokenPermissions tokenPermissions) {
        return tokenPermissions.hasPermission(Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE);
    }
}
