package uk.gov.companieshouse.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;
import uk.gov.companieshouse.service.dissolution.chips.DissolutionChipsService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResendEmailController.class)
class ResendEmailControllerTest {

    private static final String EMAIL_URI = "/dissolution-request/{company-number}/resend-email/{email-address}";
    private static final String COMPANY_NUMBER = "01777777";
    private static final String EMAIL_ADDRESS = "test@gmail.com";
    private static final String USER_ID = "1234";
    private static final String AUTHORISED_USER_HEADER = "ERIC-Authorised-User";

    @MockitoBean
    private DissolutionChipsService service;

    @MockitoBean
    private DissolutionEmailService emailService;

    @MockitoBean
    private DissolutionRepository dissolutionRepository;

    @MockitoBean
    private Dissolution dissolution;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEmailResendFunctionReturnStatusOKWhenGivenValidEmailAndActiveDissolutionCase() throws Exception {
        when(dissolutionRepository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(java.util.Optional.ofNullable(dissolution));
        mockMvc.perform(post(EMAIL_URI,COMPANY_NUMBER,EMAIL_ADDRESS).headers(createHttpHeaders()))
                .andExpect(status().isOk());
        verify(dissolutionRepository).findByCompanyNumber(COMPANY_NUMBER);
        verify(emailService).notifySignatoryToSign(dissolution, EMAIL_ADDRESS);
    }

    @Test
    void testEmailFunctionReturns404WhenGivenCompanyIsNotInActiveDissolution() throws Exception {
        mockMvc.perform(post(EMAIL_URI,COMPANY_NUMBER,EMAIL_ADDRESS).headers(createHttpHeaders()))
                .andExpect(status().isNotFound());
        verify(dissolutionRepository).findByCompanyNumber(COMPANY_NUMBER);
        verifyNoInteractions(emailService);
    }

    private HttpHeaders createHttpHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(EricConstants.ERIC_IDENTITY, USER_ID);
        httpHeaders.add(EricConstants.ERIC_IDENTITY_TYPE, SecurityConstants.API_KEY_IDENTITY_TYPE);
        httpHeaders.add(EricConstants.ERIC_AUTHORISED_KEY_ROLES, SecurityConstants.INTERNAL_USER_ROLE);
        httpHeaders.add(AUTHORISED_USER_HEADER, EMAIL_ADDRESS);
        httpHeaders.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER,
                Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE
        ));

        return httpHeaders;
    }
}
