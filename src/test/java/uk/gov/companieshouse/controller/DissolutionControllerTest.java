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
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.service.CompanyOfficerService;
import uk.gov.companieshouse.client.CompanyProfileClientImpl;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.DissolutionValidator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;
import static uk.gov.companieshouse.fixtures.CompanyProfileApiFixtures.generateCompanyProfileApi;
import static uk.gov.companieshouse.fixtures.CompanyProfileFixtures.generateCompanyProfile;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;

@RunWith(SpringRunner.class)
@WebMvcTest(DissolutionController.class)
public class DissolutionControllerTest {

    private static final String DISSOLUTION_URI = "/dissolution-request/{company-number}";

    private static final String AUTHORISED_USER_HEADER = "ERIC-Authorised-User";

    private static final String COMPANY_NUMBER = "12345678";
    private static final String USER_ID = "1234";
    private static final String OFFICER_ID = "abc123";
    private static final String EMAIL = "user@mail.com";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String PASSTHROUGH_HEADER = "passthrough";

    @MockBean
    private DissolutionService service;

    @MockBean
    private DissolutionValidator dissolutionValidator;

    @MockBean
    private CompanyProfileClientImpl companyProfileClient;

    @MockBean
    private CompanyOfficerService companyOfficerService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void submitDissolutionRequest_returnsUnauthorised_ifNoTokenPermissionsAreProvided() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                                .content(asJsonString(generateDissolutionCreateRequest()))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void submitDissolutionRequest_returnsUnauthorised_ifCompanyNumberTokenPermissionDoesNotMatchUri() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);
        headers.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER.toString(), "1234",
                Permission.Key.COMPANY_TRANSACTIONS, Permission.Value.UPDATE
        ));

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                                .content(asJsonString(generateDissolutionCreateRequest()))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifNoDirectorsAreProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();
        body.setDirectors(Collections.emptyList());

        assertPostBodyValidation(body, "{'directors':'At least 1 director must be provided'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifAnOfficerIdIsNotProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        final DirectorRequest director = generateDirectorRequest();
        director.setOfficerId(null);

        body.setDirectors(Collections.singletonList(director));

        assertPostBodyValidation(body, "{'directors[0].officerId':'must not be blank'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifAnEmailIsNotProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        final DirectorRequest director = generateDirectorRequest();
        director.setEmail(null);

        body.setDirectors(Collections.singletonList(director));

        assertPostBodyValidation(body, "{'directors[0].email':'must not be blank'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifAnInvalidEmailIsProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        final DirectorRequest director = generateDirectorRequest();
        director.setEmail("invalid email format");

        body.setDirectors(Collections.singletonList(director));

        assertPostBodyValidation(body, "{'directors[0].email':'must be a well-formed email address'}");
    }

    @Test
    public void submitDissolutionRequest_returnsUnprocessableEntity_ifAnInvalidOnBehalfNameIsProvided() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();

        final DirectorRequest director = generateDirectorRequest();
        director.setOnBehalfName("x".repeat(251));

        body.setDirectors(Collections.singletonList(director));

        assertPostBodyValidation(body, "{'directors[0].onBehalfName':'size must be between 1 and 250'}");
    }

    @Test
    public void submitDissolutionRequest_returnsNotFound_ifCompanyNotFound() throws Exception {
        when(companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(null);

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(generateDissolutionCreateRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void submitDissolutionRequest_returnsConflict_ifDissolutionAlreadyExistsForCompany() throws Exception {
        when(companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(generateCompanyProfileApi());
        when(service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER)).thenReturn(true);

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(generateDissolutionCreateRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    public void submitDissolutionRequest_returnsBadRequest_ifValidationFails() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();
        final CompanyProfileApi companyProfileApi = generateCompanyProfileApi();
        final CompanyProfile company = generateCompanyProfile();
        final Map<String, CompanyOfficer> companyDirectors = Map.of(OFFICER_ID, generateCompanyOfficer());

        when(companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);
        when(service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER)).thenReturn(false);
        when(companyOfficerService.getActiveDirectorsForCompany(COMPANY_NUMBER)).thenReturn(companyDirectors);
        when(dissolutionValidator.checkBusinessRules(eq(company), eq(companyDirectors), isA(List.class))).thenReturn(Optional.of("Some dissolution error"));

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isBadRequest());

        verify(dissolutionValidator).checkBusinessRules(eq(company), eq(companyDirectors), isA(List.class));
    }

    @Test
    public void submitDissolutionRequest_returnsInternalServerError_ifExceptionOccursWhenCreatingDissolution() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();
        final CompanyProfileApi companyProfileApi = generateCompanyProfileApi();
        final CompanyProfile company = generateCompanyProfile();
        final Map<String, CompanyOfficer> companyDirectors = Map.of(OFFICER_ID, generateCompanyOfficer());

        when(companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);
        when(service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER)).thenReturn(false);
        when(companyOfficerService.getActiveDirectorsForCompany(COMPANY_NUMBER)).thenReturn(companyDirectors);
        when(dissolutionValidator.checkBusinessRules(eq(company), eq(companyDirectors), isA(List.class))).thenReturn(Optional.empty());
        when(service.create(isA(DissolutionCreateRequest.class), eq(company), eq(companyDirectors), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL))).thenThrow(new RuntimeException());

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isInternalServerError());

        verify(service).create(isA(DissolutionCreateRequest.class), eq(company), eq(companyDirectors), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL));
    }

    @Test
    public void submitDissolutionRequest_returnsCreated_andCreateResponse_ifDissolutionIsCreatedSuccessfully() throws Exception {
        final DissolutionCreateRequest body = generateDissolutionCreateRequest();
        final DissolutionCreateResponse response = generateDissolutionCreateResponse();
        final CompanyProfileApi companyProfileApi = generateCompanyProfileApi();
        final CompanyProfile company = generateCompanyProfile();
        final Map<String, CompanyOfficer> companyDirectors = Map.of(OFFICER_ID, generateCompanyOfficer());

        when(companyProfileClient.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfileApi);
        when(service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER)).thenReturn(false);
        when(companyOfficerService.getActiveDirectorsForCompany(COMPANY_NUMBER)).thenReturn(companyDirectors);
        when(dissolutionValidator.checkBusinessRules(eq(company), eq(companyDirectors), isA(List.class))).thenReturn(Optional.empty());
        when(service.create(isA(DissolutionCreateRequest.class), eq(company), eq(companyDirectors), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL))).thenReturn(response);

        mockMvc
                .perform(
                        post(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().json(asJsonString(response)));

        verify(service).create(isA(DissolutionCreateRequest.class), eq(company), eq(companyDirectors), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL));
    }

    @Test
    public void getDissolutionRequest_returnsUnauthorised_ifNoTokenPermissionsAreProvided() throws Exception {
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
    public void getDissolutionRequest_returnsUnauthorised_ifCompanyNumberTokenPermissionDoesNotMatchUri() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);
        headers.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER.toString(), "1234",
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
    public void getDissolutionRequest_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        when(service.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

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

        when(service.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(response));

        mockMvc
                .perform(
                        get(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders()))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));
    }

    @Test
    public void patchDissolutionRequest_returnsUnauthorised_ifNoTokenPermissionsAreProvided() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);

        mockMvc
                .perform(
                        patch(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                                .content(asJsonString(generateDissolutionPatchRequest()))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void patchDissolutionRequest_returnsUnauthorised_ifCompanyNumberTokenPermissionDoesNotMatchUri() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);
        headers.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER.toString(), "1234",
                Permission.Key.COMPANY_TRANSACTIONS, Permission.Value.UPDATE
        ));

        mockMvc
                .perform(
                        patch(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                                .content(asJsonString(generateDissolutionPatchRequest()))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void patchDissolutionRequest_returnsUnprocessableEntity_ifNoOfficerIdProvided() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setOfficerId(null);

        assertPatchBodyValidation(body, "{'officerId':'must not be blank'}");
    }

    @Test
    public void patchDissolutionRequest_returnsUnprocessableEntity_ifHasApprovedIsNotTrue() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setHasApproved(false);

        assertPatchBodyValidation(body, "{'hasApproved':'must be true'}");
    }

    @Test
    public void patchDissolutionRequest_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();

        when(service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER)).thenReturn(false);

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
    public void patchDissolutionRequest_returnsBadRequest_ifDirectorNotPendingApproval() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setOfficerId(OFFICER_ID);

        when(service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER)).thenReturn(true);
        when(service.isDirectorPendingApproval(eq(COMPANY_NUMBER), eq(OFFICER_ID))).thenReturn(false);

        mockMvc
                .perform(
                        patch(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void patchDissolutionRequest_returnsUnprocessableEntity_ifIPIsBlank() throws Exception, HttpClientErrorException.UnprocessableEntity {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(null);

        assertPatchBodyValidation(body, "{'ipAddress':'must not be blank'}");
    }

    @Test
    public void patchDissolutionRequest_returnsOK_andPatchResponse_ifDissolutionIsPatchedSuccessfully() throws Exception, DissolutionNotFoundException {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final DissolutionPatchResponse response = generateDissolutionPatchResponse();

        when(service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER)).thenReturn(true);
        when(service.isDirectorPendingApproval(eq(COMPANY_NUMBER), eq(OFFICER_ID))).thenReturn(true);
        when(service.addDirectorApproval(eq(COMPANY_NUMBER), eq(USER_ID), isA(DissolutionPatchRequest.class))).thenReturn(response);

        mockMvc
                .perform(
                        patch(DISSOLUTION_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(response)));

        verify(service).addDirectorApproval(eq(COMPANY_NUMBER), eq(USER_ID), isA(DissolutionPatchRequest.class));
    }

    private void assertPostBodyValidation(DissolutionCreateRequest body, String expectedErrorJson) throws Exception {
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

    private void assertPatchBodyValidation(DissolutionPatchRequest body, String expectedErrorJson) throws Exception {
        mockMvc
                .perform(
                        patch(DISSOLUTION_URI, COMPANY_NUMBER)
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
