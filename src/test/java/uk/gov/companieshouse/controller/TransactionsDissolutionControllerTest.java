package uk.gov.companieshouse.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.DissolutionDirectorApprovalException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.InvalidTransactionStateException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.exception.TransactionNotFoundException;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalData;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateDraftResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionLinks;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.service.CompanyProfileService;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.CompanyProfileFixtures.generateCompanyProfile;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionPatchRequest;
import static uk.gov.companieshouse.fixtures.TransactionFixtures.TRANSACTION_ID;
import static uk.gov.companieshouse.model.Constants.HEADER_ERIC_REQUEST_ID;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_KEY;

@WebMvcTest(TransactionsDissolutionController.class)
class TransactionsDissolutionControllerTest {

    private static final String DISSOLUTION_URI = "/company/{company-number}/transaction/{transaction_id}/dissolution";
    private static final String DISSOLUTION_APPROVAL_URI = "/company/{company-number}/transaction/{transaction_id}/dissolution/approve";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String OFFICER_ID = "abc123";
    private static final String USER_ID = "1234";
    private static final String EMAIL = "user@example.com";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String ERIC_REQUEST_ID = "XaBcDeF12345";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String AUTHORISED_USER_HEADER = "ERIC-Authorised-User";
    private static final String ERIC_ACCESS_TOKEN_HEADER = "ERIC-Access-Token";

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private DissolutionService dissolutionService;

    @MockitoBean
    private CompanyProfileService companyProfileService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private Transaction transaction;

    @BeforeEach
    void setup() {
        transaction = TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.OPEN).withCompanyNumber(COMPANY_NUMBER).build();
        when(transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER)).thenReturn(transaction);
    }

    @Test
    void submitDraftDissolution_returnsUnauthorised_ifNoTokenPermissionsAreProvided() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID).headers(headers))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void submitDraftDissolution_returnsUnauthorised_ifCompanyNumberTokenPermissionDoesNotMatchUri() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);
        headers.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER, "1234",
                Permission.Key.COMPANY_TRANSACTIONS, Permission.Value.UPDATE
        ));

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID).headers(headers))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void submitDraftDissolution_returnsNotFound_ifCompanyNotFound() throws Exception {
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenThrow(NotFoundException.class);

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isNotFound());

        verify(dissolutionService, never()).createDraft(any(), any(), any(), any(), any());
    }

    @Test
    void submitDraftDissolution_returnsNotFound_ifTransactionNotFound() throws Exception {
        when(transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER)).thenThrow(TransactionNotFoundException.class);

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isNotFound());

        verify(dissolutionService, never()).createDraft(any(), any(), any(), any(), any());
    }

    @Test
    void submitDraftDissolution_returnsConflict_ifDraftDissolutionAlreadyExistsForUserAndCompany() throws Exception {
        final CompanyProfile companyProfile = generateCompanyProfile();
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyProfileService.isCompanyClosable(isA(CompanyProfile.class))).thenReturn(true);
        when(dissolutionService.createDraft(isA(Transaction.class), eq(companyProfile), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL)))
                .thenThrow(new ConflictException("draft dissolution already exists"));

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isConflict());
    }

    @Test
    void submitDraftDissolution_returnsConflict_ifTransactionIsNotOpen() throws Exception {
        transaction.setStatus(TransactionStatus.CLOSED);
        final CompanyProfile companyProfile = generateCompanyProfile();
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyProfileService.isCompanyClosable(isA(CompanyProfile.class))).thenReturn(true);
        when(dissolutionService.createDraft(isA(Transaction.class), eq(companyProfile), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL)))
                .thenThrow(new InvalidTransactionStateException("transaction is not open"));

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isConflict());
    }

    @Test
    void submitDraftDissolution_returnsConflict_ifTransactionIsNotAssociatedWithTheCompany() throws Exception {
        transaction.setCompanyNumber("87654321");
        final CompanyProfile companyProfile = generateCompanyProfile();
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyProfileService.isCompanyClosable(isA(CompanyProfile.class))).thenReturn(true);
        when(dissolutionService.createDraft(isA(Transaction.class), eq(companyProfile), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL)))
                .thenThrow(new InvalidTransactionStateException("transaction is not associated with the company"));

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isConflict());
    }

    @Test
    void submitDraftDissolution_returnsBadRequest_ifCompanyIsNotClosable() throws Exception {
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(generateCompanyProfile());
        when(companyProfileService.isCompanyClosable(isA(CompanyProfile.class))).thenReturn(false);

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isBadRequest());

        verify(dissolutionService, never()).createDraft(any(), any(), any(), any(), any());
    }

    @Test
    void submitDraftDissolution_returnsInternalServerError_ifExceptionOccursWhenCreatingDraftDissolution() throws Exception {
        final CompanyProfile companyProfile = generateCompanyProfile();
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyProfileService.isCompanyClosable(isA(CompanyProfile.class))).thenReturn(true);
        when(dissolutionService.createDraft(isA(Transaction.class), eq(companyProfile), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL)))
                .thenThrow(new RuntimeException("Some error occurred while creating draft dissolution"));

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void submitDraftDissolution_returnsCreated_andCreateResponse_ifDraftDissolutionIsCreatedSuccessfully() throws Exception {
        final DissolutionCreateDraftResponse response = new DissolutionCreateDraftResponse();
        response.setDissolutionId("dis-123");
        DissolutionLinks links = new DissolutionLinks();
        links.setSelf("/company/" + COMPANY_NUMBER + "/transaction/" + TRANSACTION_ID + "/dissolution");
        response.setLinks(links);
        final CompanyProfile companyProfile = generateCompanyProfile();

        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, PASSTHROUGH_HEADER)).thenReturn(companyProfile);
        when(companyProfileService.isCompanyClosable(isA(CompanyProfile.class))).thenReturn(true);
        when(dissolutionService.createDraft(isA(Transaction.class), eq(companyProfile), eq(USER_ID), eq(IP_ADDRESS), eq(EMAIL)))
                .thenReturn(response);

        mockMvc
                .perform(post(DISSOLUTION_URI, COMPANY_NUMBER, TRANSACTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isCreated())
                .andExpect(content().json(asJsonString(response)))
                .andExpect(jsonPath("$.dissolution_id").value("dis-123"))
                .andExpect(jsonPath("$.links.self").value("/company/" + COMPANY_NUMBER + "/transaction/" + TRANSACTION_ID + "/dissolution"));
    }

    @Test
    void patchDissolutionApproval_returnsUnauthorised_ifNoTokenPermissionsAreProvided() throws Exception {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);

        mockMvc
                .perform(
                        patch(DISSOLUTION_APPROVAL_URI, COMPANY_NUMBER, TRANSACTION_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                                .content(asJsonString(generateDissolutionPatchRequest()))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchDissolutionApproval_returnsUnauthorised_ifCompanyNumberTokenPermissionDoesNotMatchUri() throws Exception {
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
                        patch(DISSOLUTION_APPROVAL_URI, COMPANY_NUMBER, TRANSACTION_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                                .content(asJsonString(generateDissolutionPatchRequest()))
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patchDissolutionApproval_returnsUnprocessableEntity_ifNoOfficerIdProvided() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setOfficerId(null);

        assertPatchBodyValidation(body, "{'officerId':'must not be blank'}");
    }

    @Test
    void patchDissolutionApproval_returnsUnprocessableEntity_ifHasApprovedIsNotTrue() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setHasApproved(false);

        assertPatchBodyValidation(body, "{'hasApproved':'must be true'}");
    }

    @Test
    void patchDissolutionApproval_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        doThrow(new DissolutionNotFoundException("Dissolution not found"))
                .when(dissolutionService).addDirectorApproval(COMPANY_NUMBER, transaction, directorApprovalData);

        mockMvc
                .perform(
                        patch(DISSOLUTION_APPROVAL_URI, COMPANY_NUMBER, TRANSACTION_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                                .content(asJsonString(body))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void patchDissolutionApproval_returnsNotFound_ifTransactionNotFound() throws Exception {
        when(transactionService.getTransaction(TRANSACTION_ID, PASSTHROUGH_HEADER)).thenThrow(TransactionNotFoundException.class);

        mockMvc
                .perform(patch(DISSOLUTION_APPROVAL_URI, COMPANY_NUMBER, TRANSACTION_ID)
                        .headers(createHttpHeaders())
                        .requestAttr(TRANSACTION_KEY, transaction))
                .andExpect(status().isNotFound());

        verify(dissolutionService, never()).addDirectorApproval(any(), any(), any());
    }

    @Test
    void patchDissolutionApproval_returnsBadRequest_ifDissolutionDirectorIsNotFound() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        doThrow(new DissolutionDirectorApprovalException("Director not found"))
                .when(dissolutionService).addDirectorApproval(COMPANY_NUMBER, transaction, directorApprovalData);

        mockMvc
                .perform(
                        patch(DISSOLUTION_APPROVAL_URI, COMPANY_NUMBER, TRANSACTION_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                                .content(asJsonString(body))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchDissolutionApproval_returnsBadRequest_ifDirectorNotPendingApproval() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        doThrow(new DissolutionDirectorApprovalException("Director not pending approval"))
                .when(dissolutionService).addDirectorApproval(COMPANY_NUMBER, transaction, directorApprovalData);

        mockMvc
                .perform(
                        patch(DISSOLUTION_APPROVAL_URI, COMPANY_NUMBER, TRANSACTION_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                                .content(asJsonString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchDissolutionApproval_returnsUnprocessableEntity_ifIPIsBlank() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(null);

        assertPatchBodyValidation(body, "{'ipAddress':'must not be blank'}");
    }

    @Test
    void patchDissolutionApproval_returnsNoContent_ifDissolutionApprovalIsPatchedSuccessfully() throws Exception {
        final DissolutionPatchRequest body = generateDissolutionPatchRequest();
        body.setIpAddress(IP_ADDRESS);
        body.setOfficerId(OFFICER_ID);
        final var directorApprovalData = new DissolutionDirectorApprovalData(USER_ID, body.getOfficerId(), body.getIpAddress(), body.getHasApproved());

        mockMvc
                .perform(
                        patch(DISSOLUTION_APPROVAL_URI, COMPANY_NUMBER, TRANSACTION_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                                .content(asJsonString(body)))
                .andExpect(status().isNoContent());

        verify(dissolutionService, times(1)).addDirectorApproval(COMPANY_NUMBER, transaction, directorApprovalData);
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(EricConstants.ERIC_IDENTITY, USER_ID);
        headers.add(AUTHORISED_USER_HEADER, EMAIL);
        headers.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER, COMPANY_NUMBER,
                Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE
        ));
        headers.add(ERIC_ACCESS_TOKEN_HEADER, PASSTHROUGH_HEADER);
        headers.add(HEADER_ERIC_REQUEST_ID, ERIC_REQUEST_ID);

        return headers;
    }

    private void assertPatchBodyValidation(DissolutionPatchRequest body, String expectedErrorJson) throws Exception {
        mockMvc
                .perform(
                        patch(DISSOLUTION_APPROVAL_URI, COMPANY_NUMBER, TRANSACTION_ID)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .requestAttr(TRANSACTION_KEY, transaction)
                                .content(asJsonString(body))
                )
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_CONTENT.value()))
                .andExpect(content().json(expectedErrorJson));
    }

    private <T> String asJsonString(T body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
