package uk.gov.companieshouse.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionGetResponse;

@SuppressWarnings({"unchecked", "UastIncorrectHttpHeaderInspection"})
@WebMvcTest(DissolutionController.class)
class DissolutionDataControllerTest {

    private static final String DISSOLUTION_URI = "/dissolution/{company-number}";

    private static final String AUTHORISED_USER_HEADER = "ERIC-Authorised-User";
    private static final String ERIC_ACCESS_TOKEN_HEADER = "ERIC-Access-Token";

    private static final String COMPANY_NUMBER = "12345678";
    private static final String USER_ID = "1234";
    private static final String EMAIL = "user@mail.com";
    private static final String PASSTHROUGH_HEADER = "passthrough";

    @MockitoBean
    private DissolutionService service;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void getDissolutionRequest_returnsUnauthorised_ifNoTokenPermissionsAreProvided() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);

        mockMvc
                .perform(
                        get(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDissolutionRequest_returnsUnauthorised_ifCompanyNumberTokenPermissionDoesNotMatchUri() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);
        headers.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER, "1234",
                Permission.Key.COMPANY_TRANSACTIONS, Permission.Value.UPDATE
        ));

        mockMvc
                .perform(
                        get(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getDissolutionRequest_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        when(service.getPendingOrDraftDissolution(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.empty());

        mockMvc
                .perform(
                        get(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDissolutionRequest_returnsDissolutionInfo_ifDissolutionExists() throws Exception {
        final DissolutionGetResponse response = generateDissolutionGetResponse();

        when(service.getPendingOrDraftDissolution(USER_ID, COMPANY_NUMBER)).thenReturn(Optional.of(response));

        mockMvc
                .perform(
                        get(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders()))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));
    }

    private <T> String asJsonString(T body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders createHttpHeaders() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(EricConstants.ERIC_IDENTITY, USER_ID);
        httpHeaders.add(AUTHORISED_USER_HEADER, EMAIL);
        httpHeaders.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER,
                Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE
        ));
        httpHeaders.add(ERIC_ACCESS_TOKEN_HEADER, PASSTHROUGH_HEADER);

        return httpHeaders;
    }
}
