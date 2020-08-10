package uk.gov.companieshouse.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.service.dissolution.chips.DissolutionChipsService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SubmitControllerTest {

    private static final String SUBMIT_URI = "/dissolution-request/submit";

    @MockBean
    private DissolutionChipsService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void submitDissolutionsToChips_returnsServiceUnavailable_ifChipsIsNotAvailable() throws Exception {
        when(service.isAvailable()).thenReturn(false);

        mockMvc
                .perform(post(SUBMIT_URI))
                .andExpect(status().isServiceUnavailable());

        verify(service, never()).submitDissolutionsToChips();
    }

    @Test
    public void submitDissolutionsToChips_submitsDissolutions_returnsOk_ifChipsIsAvailable() throws Exception {
        when(service.isAvailable()).thenReturn(true);

        mockMvc
                .perform(post(SUBMIT_URI))
                .andExpect(status().isOk());

        verify(service).submitDissolutionsToChips();
    }
}
