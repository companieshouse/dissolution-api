package uk.gov.companieshouse.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.SecurityConstants;
import uk.gov.companieshouse.service.dissolution.chips.DissolutionChipsService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(SubmitController.class)
public class SubmitControllerTest {

    private static final String SUBMIT_URI = "/dissolution-request/submit";

    private static final String IDENTITY_HEADER_VALUE = "identity";

    @MockBean
    private DissolutionChipsService service;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void submitDissolutionsToChips_returnsUnauthorised_ifEricIdentityIsNotProvided() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.remove(EricConstants.ERIC_IDENTITY);

        mockMvc
                .perform(
                        post(SUBMIT_URI)
                                .headers(headers)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void submitDissolutionsToChips_returnsForbidden_ifEricIdentityTypeIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_IDENTITY_TYPE, "some-incorrect-identity-type");

        mockMvc
                .perform(
                        post(SUBMIT_URI)
                                .headers(headers)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void submitDissolutionsToChips_returnsForbidden_ifEricAuthorisedKeyRolesIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_AUTHORISED_KEY_ROLES, "some-incorrect-authorised-key-roles-value");

        mockMvc
                .perform(
                        post(SUBMIT_URI)
                                .headers(headers)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void submitDissolutionsToChips_returnsServiceUnavailable_ifChipsIsNotAvailable() throws Exception {
        when(service.isAvailable()).thenReturn(false);

        mockMvc
                .perform(
                        post(SUBMIT_URI)
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isServiceUnavailable());

        verify(service, never()).submitDissolutionsToChips();
    }

    @Test
    public void submitDissolutionsToChips_submitsDissolutions_returnsOk_ifChipsIsAvailable() throws Exception {
        when(service.isAvailable()).thenReturn(true);

        mockMvc
                .perform(
                        post(SUBMIT_URI)
                                .headers(createHttpHeaders())
                )
                .andExpect(status().isOk());

        verify(service).submitDissolutionsToChips();
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(EricConstants.ERIC_IDENTITY, IDENTITY_HEADER_VALUE);
        headers.add(EricConstants.ERIC_IDENTITY_TYPE, SecurityConstants.API_KEY_IDENTITY_TYPE);
        headers.add(EricConstants.ERIC_AUTHORISED_KEY_ROLES, SecurityConstants.INTERNAL_USER_ROLE);

        return headers;
    }
}
