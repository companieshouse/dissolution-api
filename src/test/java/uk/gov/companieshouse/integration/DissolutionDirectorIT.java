package uk.gov.companieshouse.integration;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.fixtures.DirectorApprovalTestDataBuilder.aDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorPatchRequestTestDataBuilder.aDissolutionDirectorPatchRequest;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;

class DissolutionDirectorIT extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String OFFICER_ID = "abc123";
    private static final String DIRECTOR_PATH = "/dissolution-request/{companyNumber}/directors/{directorId}";

    @Test
    void patchDirectorUpdatesDetailsAndSendsSignatoryEmail() {
        mongoTemplate.insert(aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withOnlyDirector(aDissolutionDirector().withOfficerId(OFFICER_ID).withEmail("john@doe.com"))
                .build());

        final DissolutionDirectorPatchRequest body = aDissolutionDirectorPatchRequest()
                .withEmail("New.Signatory@Example.com")
                .withOnBehalfName("Jane Doe")
                .build();

        final DissolutionDirectorPatchResponse response = RestClient.create().patch()
                .uri(baseUrl() + DIRECTOR_PATH, COMPANY_NUMBER, OFFICER_ID)
                .headers(headers -> headers.addAll(ericUserHeaders(COMPANY_NUMBER)))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(DissolutionDirectorPatchResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.getLinks().getSelf()).isEqualTo("/dissolution-request/" + COMPANY_NUMBER);

        final Dissolution saved = findDissolution();
        assertThat(saved.getData().getDirectors()).hasSize(1);
        assertThat(saved.getData().getDirectors().getFirst().getEmail()).isEqualTo("new.signatory@example.com");
        assertThat(saved.getData().getDirectors().getFirst().getOnBehalfName()).isEqualTo("Jane Doe");

        verify(emailClient).sendEmail(any());
    }

    @Test
    void patchDirectorWithUnchangedDetailsReturnsOkWithoutSendingEmail() {
        mongoTemplate.insert(aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withOnlyDirector(aDissolutionDirector()
                        .withOfficerId(OFFICER_ID)
                        .withEmail("john@doe.com")
                        .withOnBehalfName("Jane Doe"))
                .build());

        final ResponseEntity<String> response = patchDirector(COMPANY_NUMBER, OFFICER_ID,
                ericUserHeaders(COMPANY_NUMBER),
                aDissolutionDirectorPatchRequest().withEmail("john@doe.com").withOnBehalfName("Jane Doe").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void patchDirectorWithInvalidEmailReturnsUnprocessableContent() {
        mongoTemplate.insert(aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withDirector(aDissolutionDirector().withOfficerId(OFFICER_ID))
                .build());

        final ResponseEntity<String> response = patchDirector(COMPANY_NUMBER, OFFICER_ID,
                ericUserHeaders(COMPANY_NUMBER),
                aDissolutionDirectorPatchRequest().withEmail("not-an-email").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody()).contains("email");
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void patchDirectorForUnknownDirectorReturnsNotFound() {
        mongoTemplate.insert(aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withDirector(aDissolutionDirector().withOfficerId(OFFICER_ID))
                .build());

        final ResponseEntity<String> response = patchDirector(COMPANY_NUMBER, "unknown-officer",
                ericUserHeaders(COMPANY_NUMBER),
                aDissolutionDirectorPatchRequest().withEmail("new@example.com").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void patchDirectorForUnknownCompanyReturnsNotFound() {
        final ResponseEntity<String> response = patchDirector("99999999", OFFICER_ID,
                ericUserHeaders("99999999"),
                aDissolutionDirectorPatchRequest().withEmail("new@example.com").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void patchDirectorWhoAlreadyApprovedReturnsBadRequest() {
        mongoTemplate.insert(aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withOnlyDirector(aDissolutionDirector()
                        .withOfficerId(OFFICER_ID)
                        .withDirectorApproval(aDirectorApproval()))
                .build());

        final ResponseEntity<String> response = patchDirector(COMPANY_NUMBER, OFFICER_ID,
                ericUserHeaders(COMPANY_NUMBER),
                aDissolutionDirectorPatchRequest().withEmail("new@example.com").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void patchDirectorByNonApplicantReturnsBadRequest() {
        mongoTemplate.insert(aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withCreatedByEmail("applicant@example.com")
                .withDirector(aDissolutionDirector().withOfficerId(OFFICER_ID))
                .build());

        final ResponseEntity<String> response = patchDirector(COMPANY_NUMBER, OFFICER_ID,
                ericUserHeaders(COMPANY_NUMBER),
                aDissolutionDirectorPatchRequest().withEmail("new@example.com").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(emailClient, never()).sendEmail(any());
    }

    @Test
    void patchDirectorWithoutPermissionForCompanyReturnsUnauthorised() {
        mongoTemplate.insert(aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withDirector(aDissolutionDirector().withOfficerId(OFFICER_ID))
                .build());

        final ResponseEntity<String> response = patchDirector(COMPANY_NUMBER, OFFICER_ID,
                ericUserHeaders("87654321"),
                aDissolutionDirectorPatchRequest().withEmail("new@example.com").build());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(emailClient, never()).sendEmail(any());
    }

    private ResponseEntity<String> patchDirector(String companyNumber, String directorId,
                                                 HttpHeaders headers, DissolutionDirectorPatchRequest body) {
        return RestClient.create().patch()
                .uri(baseUrl() + DIRECTOR_PATH, companyNumber, directorId)
                .headers(requestHeaders -> requestHeaders.addAll(headers))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(status -> true, (request, response) -> {
                    // Error statuses are asserted by the caller.
                })
                .toEntity(String.class);
    }

    private Dissolution findDissolution() {
        return mongoTemplate.findOne(
                Query.query(Criteria.where("company.number").is(DissolutionDirectorIT.COMPANY_NUMBER)), Dissolution.class);
    }
}
