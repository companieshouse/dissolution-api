package uk.gov.companieshouse.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.api.util.security.TokenPermissions;
import uk.gov.companieshouse.exception.UnauthorisedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DissolutionTokenPermissionsInterceptorTest {

    private static final String COMPANY_NUMBER = "1234";
    private static final String TOKEN_PERMISSION_ATTRIBUTE = "token_permissions";

    private final DissolutionTokenPermissionsInterceptor interceptor = new DissolutionTokenPermissionsInterceptor();

    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    @BeforeEach
    public void setup() {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = mock(Object.class);

        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(Map.of("company-number", COMPANY_NUMBER));
    }

    @Test
    void interceptor_throwsAnUnauthorisedError_ifNoTokenPermissionsArePresent() {
        when(request.getAttribute(TOKEN_PERMISSION_ATTRIBUTE)).thenReturn(null);

        assertThrows(UnauthorisedException.class, () -> interceptor.preHandle(request, response, handler), "TokenPermissions not present in request");

        verify(request).getAttribute(TOKEN_PERMISSION_ATTRIBUTE);
    }

    @Test
    void interceptor_throwsAnUnauthorisedError_ifCompanyNumberTokenPermissionsDoesNotMatchUri() {
        TokenPermissions tokenPermissions = mock(TokenPermissions.class);

        when(request.getAttribute(TOKEN_PERMISSION_ATTRIBUTE)).thenReturn(tokenPermissions);
        when(tokenPermissions.hasPermission(Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER)).thenReturn(false);

        assertThrows(UnauthorisedException.class, () -> interceptor.preHandle(request, response, handler), "User is not authorised for company");

        verify(tokenPermissions).hasPermission(Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER);
    }

    @Test
    void interceptor_throwsAnUnauthorisedError_ifTokenPermissionsDoesNotContainRequiredPermissions() {
        TokenPermissions tokenPermissions = mock(TokenPermissions.class);

        when(request.getAttribute(TOKEN_PERMISSION_ATTRIBUTE)).thenReturn(tokenPermissions);
        when(tokenPermissions.hasPermission(Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER)).thenReturn(true);
        when(tokenPermissions.hasPermission(Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE)).thenReturn(false);

        assertThrows(UnauthorisedException.class, () -> interceptor.preHandle(request, response, handler), "User is not authorised to perform dissolution");

        verify(tokenPermissions).hasPermission(Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE);
    }

    @Test
    void interceptor_returnsTrue_ifTokenPermissionsContainsAllRequiredPermissions() {
        TokenPermissions tokenPermissions = mock(TokenPermissions.class);

        when(request.getAttribute(TOKEN_PERMISSION_ATTRIBUTE)).thenReturn(tokenPermissions);
        when(tokenPermissions.hasPermission(Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER)).thenReturn(true);
        when(tokenPermissions.hasPermission(Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE)).thenReturn(true);

        assertTrue(interceptor.preHandle(request, response, handler));
    }
}
