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
import uk.gov.companieshouse.fixtures.ChipsFixtures;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.chips.ChipsResponseService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ResponseController.class)
public class ResponseControllerTest {

    private static final String RESPONSE_URI = "/dissolution-request/response";

    private static final String IDENTITY_HEADER_VALUE = "identity";

    @MockBean
    private DissolutionService dissolutionService;

    @MockBean
    private ChipsResponseService chipsResponseService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void postDissolutionApplicationOutcome_returnsUnauthorised_ifEricIdentityIsNotProvided() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.remove(EricConstants.ERIC_IDENTITY);

        mockMvc
                .perform(
                        post(RESPONSE_URI)
                                .headers(headers)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void postDissolutionApplicationOutcome_returnsForbidden_ifEricIdentityTypeIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_IDENTITY_TYPE, "some-incorrect-identity-type");

        mockMvc
                .perform(
                        post(RESPONSE_URI)
                                .headers(headers)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void postDissolutionApplicationOutcome_returnsForbidden_ifEricAuthorisedKeyRolesIsNotCorrect() throws Exception {
        HttpHeaders headers = createHttpHeaders();
        headers.set(EricConstants.ERIC_AUTHORISED_KEY_ROLES, "some-incorrect-authorised-key-roles-value");

        mockMvc
                .perform(
                        post(RESPONSE_URI)
                                .headers(headers)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void postDissolutionApplicationOutcome_returnsNotFound_ifDissolutionDoesntExist() throws Exception {
        ChipsResponseCreateRequest chipsResponseCreateRequest = ChipsFixtures.generateChipsResponseCreateRequest();

        when(dissolutionService.doesDissolutionRequestExistForCompanyByApplicationReference(chipsResponseCreateRequest.getSubmissionReference())).thenReturn(false);

        mockMvc
                .perform(
                        post(RESPONSE_URI)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(chipsResponseCreateRequest))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    public void postDissolutionApplicationOutcome_returnsAccepted_ifDissolutionVerdictCreated() throws Exception {
        final ChipsResponseCreateRequest chipsResponseCreateRequest = ChipsFixtures.generateChipsResponseCreateRequest();

        when(dissolutionService.doesDissolutionRequestExistForCompanyByApplicationReference(chipsResponseCreateRequest.getSubmissionReference())).thenReturn(true);

        mockMvc
                .perform(
                        post(RESPONSE_URI)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .headers(createHttpHeaders())
                                .content(asJsonString(chipsResponseCreateRequest))
                )
                .andExpect(status().isAccepted());
    }

    private <T> String asJsonString(T body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders createHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add(EricConstants.ERIC_IDENTITY, IDENTITY_HEADER_VALUE);
        headers.add(EricConstants.ERIC_IDENTITY_TYPE, SecurityConstants.API_KEY_IDENTITY_TYPE);
        headers.add(EricConstants.ERIC_AUTHORISED_KEY_ROLES, SecurityConstants.INTERNAL_USER_ROLE);

        return headers;
    }
}
