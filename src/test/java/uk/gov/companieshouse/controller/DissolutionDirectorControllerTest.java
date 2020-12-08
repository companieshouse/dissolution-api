package uk.gov.companieshouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.service.dissolution.director.DissolutionDirectorService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorFixtures.generateDissolutionPatchDirectorRequest;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionDirectorPatchResponse;

@RunWith(SpringRunner.class)
@WebMvcTest(DissolutionDirectorController.class)
public class DissolutionDirectorControllerTest {

    private static final String DIRECTOR_URI = "/dissolution-request/{company-number}/directors/{director-id}";

    private static final String AUTHORISED_USER_HEADER = "ERIC-Authorised-User";

    private static final String COMPANY_NUMBER = "12345678";
    private static final String USER_ID = "1234";
    private static final String OFFICER_ID = "abc123";
    private static final String EMAIL = "user@mail.com";

    @MockBean
    private DissolutionDirectorService service;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void patchDissolutionDirectorRequest_returnsUnprocessableEntity_ifNoEmailProvided() throws Exception {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        body.setEmail(null);

        assertPatchDirectorBodyValidation(body, "{'email':'must not be blank'}");
    }

    @Test
    public void patchDissolutionDirectorRequest_returnsUnprocessableEntity_ifEmailIsWrongFormat() throws Exception {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();
        body.setEmail("wrongemail");

        assertPatchDirectorBodyValidation(body, "{'email':'must be a well-formed email address'}");
    }

    @Test
    public void patchDissolutionDirectorRequest_returnsNotFound_ifDirectorOrDissolutionDoesntExist() throws Exception {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();

        when(service.doesDirectorExist(COMPANY_NUMBER, OFFICER_ID)).thenReturn(false);

        mockMvc
                .perform(
                        patch(DIRECTOR_URI, COMPANY_NUMBER, OFFICER_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void patchDissolutionDirectorRequest_returnsBadRequest_ifDirectorNotPendingApproval() throws Exception {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();

        when(service.doesDirectorExist(COMPANY_NUMBER, OFFICER_ID)).thenReturn(true);
        when(service.checkPatchDirectorConstraints(COMPANY_NUMBER, OFFICER_ID, EMAIL)).thenReturn(Optional.of("Director is not pending approval"));

        mockMvc
                .perform(
                        patch(DIRECTOR_URI, COMPANY_NUMBER, OFFICER_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void patchDissolutionDirectorRequest_returnsBadRequest_ifRequesterIsNotApplicant() throws Exception {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();

        when(service.doesDirectorExist(COMPANY_NUMBER, OFFICER_ID)).thenReturn(true);
        when(service.checkPatchDirectorConstraints(COMPANY_NUMBER, OFFICER_ID, EMAIL)).thenReturn(Optional.of("Only the applicant can update signatory"));

        mockMvc
                .perform(
                        patch(DIRECTOR_URI, COMPANY_NUMBER, OFFICER_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void patchDissolutionDirectorRequest_returnsOK_andPatchResponse_ifDirectorIsPatchedSuccessfully() throws Exception, DissolutionNotFoundException {
        final DissolutionDirectorPatchRequest body = generateDissolutionPatchDirectorRequest();

        final DissolutionDirectorPatchResponse response = generateDissolutionDirectorPatchResponse();

        when(service.doesDirectorExist(COMPANY_NUMBER, OFFICER_ID)).thenReturn(true);
        when(service.checkPatchDirectorConstraints(COMPANY_NUMBER, OFFICER_ID, EMAIL)).thenReturn(Optional.empty());

        when(service.updateSignatory(eq(COMPANY_NUMBER), isA(DissolutionDirectorPatchRequest.class), eq(OFFICER_ID))).thenReturn(response);

        mockMvc
                .perform(
                        patch(DIRECTOR_URI, COMPANY_NUMBER, OFFICER_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));

        verify(service).updateSignatory(eq(COMPANY_NUMBER), isA(DissolutionDirectorPatchRequest.class), eq(OFFICER_ID));
    }

    private void assertPatchDirectorBodyValidation(DissolutionDirectorPatchRequest body, String expectedErrorJson) throws Exception {
        mockMvc
                .perform(
                        patch(DIRECTOR_URI, COMPANY_NUMBER, OFFICER_ID)
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
        httpHeaders.add(EricConstants.ERIC_IDENTITY, USER_ID);
        httpHeaders.add(AUTHORISED_USER_HEADER, EMAIL);
        httpHeaders.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER.toString(), COMPANY_NUMBER,
                Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE
        ));

        return httpHeaders;
    }
}
