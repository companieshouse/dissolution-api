package uk.gov.companieshouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
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

    private static final String PAYMENT_URI = "/dissolution-request/{company-number}/payment";
    private static final String COMPANY_NUMBER = "12345678";

    @MockBean
    private DissolutionService dissolutionService;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getPaymentUIDataRequest_returnsPaymentUIData_ifRequestIsValid() throws Exception {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        final PaymentGetResponse paymentGetResponse = generatePaymentGetResponse(dissolutionGetResponse.getETag(), COMPANY_NUMBER);

        when(dissolutionService.get(COMPANY_NUMBER)).thenReturn(Optional.of(dissolutionGetResponse));
        when(paymentService.get(dissolutionGetResponse.getETag(), COMPANY_NUMBER)).thenReturn(paymentGetResponse);

        mockMvc
                .perform(
                        get(PAYMENT_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(asJsonString(paymentGetResponse)));
    }

    @Test
    public void getPaymentUIDataRequest_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        when(dissolutionService.get(COMPANY_NUMBER)).thenReturn(Optional.empty());

        mockMvc
                .perform(
                        get(PAYMENT_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void patchPaymentDataRequest_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        final PaymentPatchRequest body = generatePaymentPatchRequest();

        when(dissolutionService.get(COMPANY_NUMBER)).thenReturn(Optional.empty());

        mockMvc
                .perform(
                        patch(PAYMENT_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void patchPaymentDataRequest_returnsBadRequest_ifDissolutionStatusIsNotPendingPayment() throws Exception {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PENDING_APPROVAL);

        final PaymentPatchRequest body = generatePaymentPatchRequest();

        when(dissolutionService.get(COMPANY_NUMBER)).thenReturn(Optional.of(dissolutionGetResponse));

        mockMvc
                .perform(
                        patch(PAYMENT_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void patchPaymentDataRequest_returnsOK_updatesPaymentInfo_PaidPaymentIsProvided() throws Exception {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PENDING_PAYMENT);

        final PaymentPatchRequest body = generatePaymentPatchRequest();
        body.setStatus(PaymentStatus.PAID);

        when(dissolutionService.get(COMPANY_NUMBER)).thenReturn(Optional.of(dissolutionGetResponse));

        mockMvc
                .perform(
                        patch(PAYMENT_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                )
                .andExpect(status().isOk());

        verify(dissolutionService).updatePaymentAndSubmissionStatus(isA(PaymentPatchRequest.class), eq(COMPANY_NUMBER));
    }

    @Test
    public void patchPaymentDataRequest_returnsOK_doesNotUpdatePaymentInfo_ifFailedPaymentIsProvided() throws Exception {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PENDING_PAYMENT);

        final PaymentPatchRequest body = generatePaymentPatchRequest();
        body.setStatus(PaymentStatus.FAILED);

        when(dissolutionService.get(COMPANY_NUMBER)).thenReturn(Optional.of(dissolutionGetResponse));

        mockMvc
                .perform(
                        patch(PAYMENT_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                )
                .andExpect(status().isOk());

        verify(dissolutionService, never()).updatePaymentAndSubmissionStatus(isA(PaymentPatchRequest.class), eq(COMPANY_NUMBER));
    }

    @Test
    public void patchPaymentDataRequest_returnsOK_doesNotUpdatePaymentInfo_ifCancelledPaymentIsProvided() throws Exception {
        final DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PENDING_PAYMENT);

        final PaymentPatchRequest body = generatePaymentPatchRequest();
        body.setStatus(PaymentStatus.CANCELLED);

        when(dissolutionService.get(COMPANY_NUMBER)).thenReturn(Optional.of(dissolutionGetResponse));

        mockMvc
                .perform(
                        patch(PAYMENT_URI, COMPANY_NUMBER)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(asJsonString(body))
                )
                .andExpect(status().isOk());

        verify(dissolutionService, never()).updatePaymentAndSubmissionStatus(isA(PaymentPatchRequest.class), eq(COMPANY_NUMBER));
    }

    private <T> String asJsonString(T body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
