package uk.gov.companieshouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.model.dto.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.PaymentGetResponse;
import uk.gov.companieshouse.service.DissolutionService;
import uk.gov.companieshouse.service.PaymentService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionGetResponse;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentGetResponse;

@SpringBootTest
@AutoConfigureMockMvc
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

    private <T> String asJsonString(T body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
