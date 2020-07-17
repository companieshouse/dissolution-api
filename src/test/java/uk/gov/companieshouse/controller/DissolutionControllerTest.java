package uk.gov.companieshouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.model.dto.dissolution.*;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DissolutionControllerTest {

    private static final String DISSOLUTION_URI = "/dissolution-request/{company-number}";

    private static final String IDENTITY_HEADER = "ERIC-identity";
    private static final String AUTHORISED_USER_HEADER = "ERIC-Authorised-User";

    private static final String COMPANY_NUMBER = "12345678";
    private static final String USER_ID = "1234";
    private static final String EMAIL = "user@mail.com";
    private static final String IP_ADDRESS = "127.0.0.1";

    @MockBean
    private DissolutionService service;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void submitDissolutionRequest_returnsBadRequest_ifEricIdentityHeaderIsNotProvided() throws Exception {
        final HttpHeaders headers = new HttpHeaders() {{
            add(AUTHORISED_USER_HEADER, EMAIL);
        }};

        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        assertHeadersValidation(headers, "Missing request header 'ERIC-identity' for method parameter of type String");
    }

    @Test
    public void submitDissolutionRequest_returnsBadRequest_ifEricAuthorisedUserHeaderIsNotProvided() throws Exception {
        final HttpHeaders headers = new HttpHeaders() {{
            add(IDENTITY_HEADER, USER_ID);
        }};

        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        assertHeadersValidation(headers, "Missing request header 'ERIC-Authorised-User' for method parameter of type String");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifNoDirectorsAreProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();
        body.setDirectors(Collections.emptyList());

        assertBodyValidation(body, "{'directors':'At least 1 director must be provided'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifANameIsNotProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        final DirectorRequest director = generateDirectorRequest();
        director.setName(null);

        body.setDirectors(Collections.singletonList(director));

        assertBodyValidation(body, "{'directors[0].name':'must not be blank'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifAnEmailIsNotProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        final DirectorRequest director = generateDirectorRequest();
        director.setEmail(null);

        body.setDirectors(Collections.singletonList(director));

        assertBodyValidation(body, "{'directors[0].email':'must not be blank'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifAnInvalidEmailIsProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        final DirectorRequest director = generateDirectorRequest();
        director.setEmail("invalid email format");

        body.setDirectors(Collections.singletonList(director));

        assertBodyValidation(body, "{'directors[0].email':'must be a well-formed email address'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifAnInvalidNameIsProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        final DirectorRequest director = generateDirectorRequest();
        director.setName("x".repeat(251));

        body.setDirectors(Collections.singletonList(director));

        assertBodyValidation(body, "{'directors[0].name':'size must be between 1 and 250'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifAnInvalidOnBehalfNameIsProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        final DirectorRequest director = generateDirectorRequest();
        director.setOnBehalfName("x".repeat(251));

        body.setDirectors(Collections.singletonList(director));

        assertBodyValidation(body, "{'directors[0].onBehalfName':'size must be between 1 and 250'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnauthorised_ifEricIdentityHeaderIsBlank() throws Exception {
        final HttpHeaders headers = new HttpHeaders() {{
            add(IDENTITY_HEADER, "");
            add(AUTHORISED_USER_HEADER, EMAIL);
        }};

        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                                .content(asJsonString(body))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void submitDissolutionRequest_returnsConflict_ifDissolutionAlreadyExistsForCompany() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        when(service.doesDissolutionRequestExistForCompany(COMPANY_NUMBER)).thenReturn(true);

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    public void submitDissolutionRequest_returnsCreated_andCreateResponse_ifDissolutionIsCreatedSuccessfully() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();
        final DissolutionCreateResponse response = generateDissolutionCreateResponse();

        when(service.doesDissolutionRequestExistForCompany(COMPANY_NUMBER)).thenReturn(false);
        when(service.create(isA(DissolutionCreateRequest.class), eq(COMPANY_NUMBER), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL))).thenReturn(response);

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().json(asJsonString(response)));

        verify(service).create(isA(DissolutionCreateRequest.class), eq(COMPANY_NUMBER), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL));
    }

    @Test
    public void submitDissolutionRequest_returnsInternalServerError_ifExceptionOccursWhenCreatingDissolution() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        when(service.doesDissolutionRequestExistForCompany(COMPANY_NUMBER)).thenReturn(false);
        when(service.create(isA(DissolutionCreateRequest.class), eq(COMPANY_NUMBER), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL))).thenThrow(new RuntimeException());

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isInternalServerError());

        verify(service).create(isA(DissolutionCreateRequest.class), eq(COMPANY_NUMBER), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL));
    }

    @Test
    public void getDissolutionRequest_returnsUnauthorised_ifEricIdentityHeaderIsBlank() throws Exception {
        final HttpHeaders headers = new HttpHeaders() {{
            add(IDENTITY_HEADER, "");
            add(AUTHORISED_USER_HEADER, EMAIL);
        }};

        mockMvc
                .perform(
                        get(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getDissolutionRequest_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        when(service.get(COMPANY_NUMBER)).thenReturn(Optional.empty());

        mockMvc
                .perform(
                        get(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getDissolutionRequest_returnsDissolutionInfo_ifDissolutionExists() throws Exception {
        final DissolutionGetResponse response = generateDissolutionGetResponse();

        when(service.get(COMPANY_NUMBER)).thenReturn(Optional.of(response));

        mockMvc
                .perform(
                        get(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders()))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));
    }

    @Test
    public void patchDissolutionRequest_returnsUnauthorised_ifEricIdentityHeaderIsBlank() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();

        final HttpHeaders headers = new HttpHeaders() {{
            add(IDENTITY_HEADER, "");
            add(AUTHORISED_USER_HEADER, EMAIL);
        }};

        mockMvc
                .perform(
                        patch(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                                .content(asJsonString(body))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void patchDissolutionRequest_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();

        when(service.doesDissolutionRequestExistForCompany(COMPANY_NUMBER)).thenReturn(false);

        mockMvc
                .perform(
                        patch(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void patchDissolutionRequest_returnsOK_andPatchResponse_ifDissolutionIsPatchedSuccessfully() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        final DissolutionPatchResponse response = generateDissolutionPatchResponse();

        when(service.doesDissolutionRequestExistForCompany(COMPANY_NUMBER)).thenReturn(true);
        when(service.isDirectorPendingApproval(eq(COMPANY_NUMBER), eq(EMAIL))).thenReturn(true);
        when(service.addDirectorApproval(eq(COMPANY_NUMBER), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL))).thenReturn(response);

        mockMvc
                .perform(
                        patch(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));

        verify(service).addDirectorApproval(eq(COMPANY_NUMBER), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL));
    }

    @Test
    public void patchDissolutionRequest_returnsBadRequest_ifDirectorNotPendingApproval() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();

        when(service.doesDissolutionRequestExistForCompany(COMPANY_NUMBER)).thenReturn(true);
        when(service.isDirectorPendingApproval(eq(COMPANY_NUMBER), eq(EMAIL))).thenReturn(false);

        mockMvc
                .perform(
                        patch(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isBadRequest());
    }

    private void assertHeadersValidation(HttpHeaders headers, String expectedReason) throws Exception {
        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                                .content(asJsonString(generateDissolutionCreateRequest()))
                )
                .andExpect(status().isBadRequest())
                .andExpect(status().reason(expectedReason));
    }

    private void assertBodyValidation(DissolutionCreateRequest body, String expectedErrorJson) throws Exception {
        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body))
                )
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().json(expectedErrorJson));
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
        httpHeaders.add(IDENTITY_HEADER, USER_ID);
        httpHeaders.add(AUTHORISED_USER_HEADER, EMAIL);

        return httpHeaders;
    }
}
