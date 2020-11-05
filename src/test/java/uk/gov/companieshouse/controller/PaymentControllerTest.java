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
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.PaymentStatus;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.payment.PaymentService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionGetResponse;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentGetResponse;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentPatchRequest;

@RunWith(SpringRunner.class)
@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    private static final String PAYMENT_URI = "/dissolution-request/{application-reference}/payment";
    private static final String APPLICATION_REFERENCE = "12345678";

    private static final String IDENTITY_HEADER_VALUE = "identity";

    @MockBean
    private DissolutionService dissolutionService;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getPaymentUIDataRequest_returnsUnauthorised_ifEricIdentityIsNotProvided() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.remove(EricConstants.ERIC_IDENTITY);

        mockMvc
                .perform(
                        get(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getPaymentUIDataRequest_returnsForbidden_ifEricIdentityTypeIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_IDENTITY_TYPE, "some-incorrect-identity-type");

        mockMvc
                .perform(
                        get(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void getPaymentUIDataRequest_returnsForbidden_ifEricAuthorisedKeyRolesIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_AUTHORISED_KEY_ROLES, "some-incorrect-authorised-key-roles-value");

        mockMvc
                .perform(
                        get(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void getPaymentUIDataRequest_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        when(dissolutionService.getByApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.empty());

        mockMvc
                .perform(
                        get(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void getPaymentUIDataRequest_returnsPaymentUIData_ifRequestIsValid() throws Exception {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        final PaymentGetResponse paymentGetResponse = generatePaymentGetResponse(dissolutionGetResponse.getETag(), APPLICATION_REFERENCE);

        when(dissolutionService.getByApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(dissolutionGetResponse));
        when(paymentService.get(dissolutionGetResponse)).thenReturn(paymentGetResponse);

        mockMvc
                .perform(
                        get(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(paymentGetResponse)));
    }

    @Test
    public void patchPaymentDataRequest_returnsUnauthorised_ifEricIdentityIsNotProvided() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.remove(EricConstants.ERIC_IDENTITY);

        mockMvc
                .perform(
                        patch(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void patchPaymentDataRequest_returnsForbidden_ifEricIdentityTypeIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_IDENTITY_TYPE, "some-incorrect-identity-type");

        mockMvc
                .perform(
                        patch(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void patchPaymentDataRequest_returnsForbidden_ifEricAuthorisedKeyRolesIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_AUTHORISED_KEY_ROLES, "some-incorrect-authorised-key-roles-value");

        mockMvc
                .perform(
                        patch(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(headers)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void patchPaymentDataRequest_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        final PaymentPatchRequest body = generatePaymentPatchRequest();

        when(dissolutionService.getByApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.empty());

        mockMvc
                .perform(
                        patch(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void patchPaymentDataRequest_returnsBadRequest_ifDissolutionStatusIsNotPendingPayment() throws Exception {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PENDING_APPROVAL);

        final PaymentPatchRequest body = generatePaymentPatchRequest();

        when(dissolutionService.getByApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(dissolutionGetResponse));

        mockMvc
                .perform(
                        patch(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void patchPaymentDataRequest_returnsNoContent_updatesPaymentInfo_PaidPaymentIsProvided() throws Exception, DissolutionNotFoundException {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PENDING_PAYMENT);

        final PaymentPatchRequest body = generatePaymentPatchRequest();
        body.setStatus(PaymentStatus.PAID);

        when(dissolutionService.getByApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(dissolutionGetResponse));

        mockMvc
                .perform(
                        patch(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isNoContent());

        verify(dissolutionService).handlePayment(isA(PaymentPatchRequest.class), eq(APPLICATION_REFERENCE));
    }

    @Test
    public void patchPaymentDataRequest_returnsNoContent_doesNotUpdatePaymentInfo_ifFailedPaymentIsProvided() throws Exception, DissolutionNotFoundException {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PENDING_PAYMENT);

        final PaymentPatchRequest body = generatePaymentPatchRequest();
        body.setStatus(PaymentStatus.FAILED);

        when(dissolutionService.getByApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(dissolutionGetResponse));

        mockMvc
                .perform(
                        patch(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isNoContent());

        verify(dissolutionService, never()).handlePayment(isA(PaymentPatchRequest.class), eq(APPLICATION_REFERENCE));
    }

    @Test
    public void patchPaymentDataRequest_returnsNoContent_doesNotUpdatePaymentInfo_ifCancelledPaymentIsProvided() throws Exception, DissolutionNotFoundException {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PENDING_PAYMENT);

        final PaymentPatchRequest body = generatePaymentPatchRequest();
        body.setStatus(PaymentStatus.CANCELLED);

        when(dissolutionService.getByApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(dissolutionGetResponse));

        mockMvc
                .perform(
                        patch(PAYMENT_URI, APPLICATION_REFERENCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isNoContent());

        verify(dissolutionService, never()).handlePayment(isA(PaymentPatchRequest.class), eq(APPLICATION_REFERENCE));
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(EricConstants.ERIC_IDENTITY, IDENTITY_HEADER_VALUE);
        headers.add(EricConstants.ERIC_IDENTITY_TYPE, SecurityConstants.API_KEY_IDENTITY_TYPE);
        headers.add(EricConstants.ERIC_AUTHORISED_KEY_ROLES, SecurityConstants.INTERNAL_USER_ROLE);

        return headers;
    }

    private <T> String asJsonString(T body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
