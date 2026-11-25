package uk.gov.companieshouse.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.companieshouse.model.Constants.DISSOLUTION_BASE_URI_PATTERN;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.HEADER_ERIC_REQUEST_ID;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.util.security.EricConstants;
import uk.gov.companieshouse.api.util.security.Permission;
import uk.gov.companieshouse.fixtures.CompanyOfficerFixtures;
import uk.gov.companieshouse.fixtures.CompanyProfileApiFixtures;
import uk.gov.companieshouse.fixtures.CreatedByTestDataBuilder;
import uk.gov.companieshouse.fixtures.DirectorApprovalTestDataBuilder;
import uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficersResponse;
import uk.gov.companieshouse.model.enums.CompanyStatus;
import uk.gov.companieshouse.model.enums.DissolutionStatus;

class TransactionsDissolutionIT extends AbstractIntegrationTest {

    private static final String COMPANY_NUMBER = "10001";
    private static final String OTHER_COMPANY_NUMBER = "99999";
    private static final String TRANSACTION_ID = TransactionFixtures.TRANSACTION_ID;
    private static final String OTHER_USER_ID = "someone-else";

    private static final String ERIC_REQUEST_ID = "XaBcDeF12345";
    private static final String ERIC_IDENTITY_VALUE = "user123";
    private static final String ERIC_EMAIL = "user@mail.com";
    private static final String ERIC_ACCESS_TOKEN = "{\"token_type\":\"Bearer\",\"access_token\":\"test-token\",\"expires_in\":3600}";
    private static final String FORWARDED_AUTHORIZATION = "Bearer test-token";

    private static final String DISSOLUTION_PATH = "/company/{companyNumber}/transactions/{transactionId}/dissolution";
    private static final String APPROVE_PATH = DISSOLUTION_PATH + "/approve";
    private static final String INITIATION_PATH = DISSOLUTION_PATH + "/initiation";
    private static final String SIGNATORY_PATH = DISSOLUTION_PATH + "/signatories/{officerId}";
    private static final String RESEND_NOTIFICATION_PATH = SIGNATORY_PATH + "/signature-notification";

    private static final String TRANSACTION_URI = "/transactions/" + TRANSACTION_ID;
    private static final String PRIVATE_TRANSACTION_URI = "/private/transactions/" + TRANSACTION_ID;
    private static final String COMPANY_PROFILE_URI = "/company/" + COMPANY_NUMBER;
    private static final String OFFICERS_URI = "/company/" + COMPANY_NUMBER + "/officers";
    private static final String SEND_EMAIL_URI = "/send-email";
    private static final String DOCUMENT_RENDER_URI = "/document-render/store";

    private static final String OFFICER_ONE = "abc123";
    private static final String OFFICER_ONE_EMAIL = "john@doe.com";
    private static final String OFFICER_TWO = "def456";
    private static final String OFFICER_TWO_EMAIL = "fred@mercure.com";

    @BeforeEach
    void stubEmailApi() {
        stubFor(post(SEND_EMAIL_URI).willReturn(aResponse().withStatus(200)));
    }

    @Nested
    @DisplayName("Authorisation")
    class Authorisation {

        static Stream<Arguments> endpoints() {
            return Stream.of(
                    Arguments.of(HttpMethod.POST, DISSOLUTION_PATH, new Object[]{}),
                    Arguments.of(HttpMethod.PATCH, APPROVE_PATH, new Object[]{}),
                    Arguments.of(HttpMethod.POST, INITIATION_PATH, new Object[]{}),
                    Arguments.of(HttpMethod.POST, RESEND_NOTIFICATION_PATH, new Object[]{OFFICER_ONE}),
                    Arguments.of(HttpMethod.PATCH, SIGNATORY_PATH, new Object[]{OFFICER_ONE}));
        }

        @ParameterizedTest(name = "{0} {1}")
        @MethodSource("endpoints")
        @DisplayName("returns 401 and calls no downstream services when ERIC headers are missing")
        void returns401WithoutEricHeaders(HttpMethod method, String path, Object[] extraUriVars) {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());

            client.method(method).uri(path, uriVars(extraUriVars))
                    .exchange()
                    .expectStatus().isUnauthorized();

            assertDissolutionUntouched();
        }

        @ParameterizedTest(name = "{0} {1}")
        @MethodSource("endpoints")
        @DisplayName("returns 401 and calls no downstream services when the user is not authorised for the company")
        void returns401WhenUserNotAuthorisedForCompany(HttpMethod method, String path, Object[] extraUriVars) {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());

            client.method(method).uri(path, uriVars(extraUriVars))
                    .headers(h -> h.addAll(ericUserHeaders(ERIC_IDENTITY_VALUE, OTHER_COMPANY_NUMBER)))
                    .exchange()
                    .expectStatus().isUnauthorized();

            assertDissolutionUntouched();
        }

        private void assertDissolutionUntouched() {
            final Dissolution stored = findOnlyDissolution();
            assertThat(stored.getStatus()).isEqualTo(DissolutionStatus.PENDING);
            assertThat(stored.getData().getDirectors())
                    .extracting(DissolutionDirector::getEmail, DissolutionDirector::hasDirectorApproval)
                    .containsExactlyInAnyOrder(tuple(OFFICER_ONE_EMAIL, false), tuple(OFFICER_TWO_EMAIL, false));
            verify(0, anyRequestedFor(anyUrl()));
        }
    }

    @Nested
    @DisplayName("POST " + DISSOLUTION_PATH)
    class CreateDraftDissolution {

        @Test
        @DisplayName("returns 201, persists a draft dissolution and links it to the transaction")
        void returns201AndPersistsDraftDissolution() {
            stubTransaction(openTransaction());
            stubCompanyProfile(CompanyProfileApiFixtures.generateCompanyProfileApi());
            stubFor(patch(PRIVATE_TRANSACTION_URI).willReturn(noContent()));

            authorisedPost(DISSOLUTION_PATH)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.dissolution_id").isNotEmpty()
                    .jsonPath("$.links.self").isEqualTo(String.format(DISSOLUTION_BASE_URI_PATTERN, COMPANY_NUMBER, TRANSACTION_ID));

            assertThat(mongoTemplate.findAll(Dissolution.class)).singleElement().satisfies(dissolution -> {
                assertThat(dissolution.getStatus()).isEqualTo(DissolutionStatus.DRAFT);
                assertThat(dissolution.getCompany().getNumber()).isEqualTo(COMPANY_NUMBER);
                assertThat(dissolution.getTransactionId()).isEqualTo(TRANSACTION_ID);
                assertThat(dissolution.getCreatedBy().getUserId()).isEqualTo(ERIC_IDENTITY_VALUE);
                assertThat(dissolution.getCreatedBy().getEmail()).isEqualTo(ERIC_EMAIL);
            });
            verify(1, getRequestedFor(urlEqualTo(TRANSACTION_URI)).withHeader(HttpHeaders.AUTHORIZATION, equalTo(FORWARDED_AUTHORIZATION)));
            verify(1, getRequestedFor(urlEqualTo(COMPANY_PROFILE_URI)).withHeader(HttpHeaders.AUTHORIZATION, equalTo(FORWARDED_AUTHORIZATION)));
            verify(1, patchRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)).withHeader(HttpHeaders.AUTHORIZATION, equalTo(FORWARDED_AUTHORIZATION))
                    .withRequestBody(matchingJsonPath("$.resources['" + String.format(DISSOLUTION_BASE_URI_PATTERN, COMPANY_NUMBER, TRANSACTION_ID) + "'].kind", equalTo(FILING_KIND_DS01))));
        }

        @Test
        @DisplayName("returns 404 when the transaction does not exist")
        void returns404WhenTransactionNotFound() {
            stubFor(get(TRANSACTION_URI).willReturn(notFound()));

            authorisedPost(DISSOLUTION_PATH)
                    .exchange()
                    .expectStatus().isNotFound();

            assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
            verify(0, getRequestedFor(urlEqualTo(COMPANY_PROFILE_URI)));
        }

        @Test
        @DisplayName("returns 500 when the transactions API fails")
        void returns500WhenTransactionApiFails() {
            stubFor(get(TRANSACTION_URI).willReturn(serverError()));

            authorisedPost(DISSOLUTION_PATH)
                    .exchange()
                    .expectStatus().is5xxServerError();

            assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
        }

        @Test
        @DisplayName("returns 404 when the company profile does not exist")
        void returns404WhenCompanyNotFound() {
            stubTransaction(openTransaction());
            stubFor(get(COMPANY_PROFILE_URI).willReturn(notFound()));

            authorisedPost(DISSOLUTION_PATH)
                    .exchange()
                    .expectStatus().isNotFound();

            assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
            verify(0, patchRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)));
        }

        @Test
        @DisplayName("returns 400 when the company is not closable")
        void returns400WhenCompanyNotClosable() {
            final CompanyProfileApi dissolvedCompany = CompanyProfileApiFixtures.generateCompanyProfileApi();
            dissolvedCompany.setCompanyStatus(CompanyStatus.DISSOLVED.getValue());
            stubTransaction(openTransaction());
            stubCompanyProfile(dissolvedCompany);

            authorisedPost(DISSOLUTION_PATH)
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
            verify(0, patchRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)));
        }

        @Test
        @DisplayName("returns 409 when the transaction is not open")
        void returns409WhenTransactionIsNotOpen() {
            stubTransaction(transaction(TransactionStatus.CLOSED));
            stubCompanyProfile(CompanyProfileApiFixtures.generateCompanyProfileApi());

            authorisedPost(DISSOLUTION_PATH)
                    .exchange()
                    .expectStatus().isEqualTo(409);

            assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
            verify(0, patchRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)));
        }

        @Test
        @DisplayName("returns 409 when a draft dissolution already exists for the user and company")
        void returns409WhenDraftAlreadyExists() {
            final Dissolution existing = insertDissolution(DissolutionStatus.DRAFT, aDissolution());
            stubTransaction(openTransaction());
            stubCompanyProfile(CompanyProfileApiFixtures.generateCompanyProfileApi());

            authorisedPost(DISSOLUTION_PATH)
                    .exchange()
                    .expectStatus().isEqualTo(409);

            assertThat(mongoTemplate.findAll(Dissolution.class)).singleElement()
                    .extracting(Dissolution::getId).isEqualTo(existing.getId());
            verify(0, patchRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)));
        }

        @Test
        @DisplayName("returns 500 and rolls back the draft when the transaction cannot be updated")
        void returns500AndRollsBackWhenTransactionPatchFails() {
            stubTransaction(openTransaction());
            stubCompanyProfile(CompanyProfileApiFixtures.generateCompanyProfileApi());
            stubFor(patch(PRIVATE_TRANSACTION_URI).willReturn(serverError()));

            authorisedPost(DISSOLUTION_PATH)
                    .exchange()
                    .expectStatus().is5xxServerError();

            verify(1, patchRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)));
            assertThat(mongoTemplate.findAll(Dissolution.class)).isEmpty();
        }
    }

    @Nested
    @DisplayName("PATCH " + APPROVE_PATH)
    class ApproveDissolution {

        @Test
        @DisplayName("returns 204 and records the approval while other signatories are outstanding")
        void returns204AndRecordsDirectorApproval() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            authorisedPatch(APPROVE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(approvalBody(OFFICER_ONE, true))
                    .exchange()
                    .expectStatus().isNoContent();

            final Dissolution stored = findOnlyDissolution();
            assertThat(stored.getStatus()).isEqualTo(DissolutionStatus.PENDING);
            assertThat(findSignatory(stored, OFFICER_ONE).hasDirectorApproval()).isTrue();
            assertThat(findSignatory(stored, OFFICER_TWO).hasDirectorApproval()).isFalse();
            verify(0, postRequestedFor(urlEqualTo(DOCUMENT_RENDER_URI)));
            verify(0, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)));
        }

        @Test
        @DisplayName("returns 204, submits the dissolution and generates a certificate on final approval")
        void returns204AndSubmitsOnFinalApproval() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution().withDirectors(
                    signatory(OFFICER_ONE, OFFICER_ONE_EMAIL).withDirectorApproval(DirectorApprovalTestDataBuilder.aDirectorApproval()),
                    signatory(OFFICER_TWO, OFFICER_TWO_EMAIL)));
            stubTransaction(openTransaction());
            stubFor(post(DOCUMENT_RENDER_URI).willReturn(aResponse()
                    .withStatus(201)
                    .withHeader("Location", "s3://some-pdf-bucket/certificate.pdf")));

            authorisedPatch(APPROVE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(approvalBody(OFFICER_TWO, true))
                    .exchange()
                    .expectStatus().isNoContent();

            final Dissolution stored = findOnlyDissolution();
            assertThat(stored.getStatus()).isEqualTo(DissolutionStatus.SUBMITTED);
            assertThat(findSignatory(stored, OFFICER_TWO).hasDirectorApproval()).isTrue();
            assertThat(stored.getCertificate()).isNotNull();
            verify(1, postRequestedFor(urlEqualTo(DOCUMENT_RENDER_URI)));
        }

        @Test
        @DisplayName("returns 404 when there is no pending dissolution")
        void returns404WhenNoPendingDissolution() {
            stubTransaction(openTransaction());

            authorisedPatch(APPROVE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(approvalBody(OFFICER_ONE, true))
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("returns 400 when the officer is not a signatory")
        void returns400WhenOfficerNotSignatory() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            authorisedPatch(APPROVE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(approvalBody("unknown999", true))
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(findOnlyDissolution().getData().getDirectors())
                    .noneMatch(DissolutionDirector::hasDirectorApproval);
        }

        @Test
        @DisplayName("returns 409 when the transaction is not open")
        void returns409WhenTransactionNotOpen() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(transaction(TransactionStatus.CLOSED));

            authorisedPatch(APPROVE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(approvalBody(OFFICER_ONE, true))
                    .exchange()
                    .expectStatus().isEqualTo(409);

            assertThat(findSignatory(findOnlyDissolution(), OFFICER_ONE).hasDirectorApproval()).isFalse();
        }

        @Test
        @DisplayName("returns 422 with field errors when the body is invalid")
        void returns422WhenBodyInvalid() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            authorisedPatch(APPROVE_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("officer_id", "", "has_approved", false))
                    .exchange()
                    .expectStatus().isEqualTo(422)
                    .expectBody()
                    .jsonPath("$.officerId").exists()
                    .jsonPath("$.hasApproved").exists()
                    .jsonPath("$.ipAddress").exists();

            assertThat(findSignatory(findOnlyDissolution(), OFFICER_ONE).hasDirectorApproval()).isFalse();
        }
    }

    @Nested
    @DisplayName("POST " + INITIATION_PATH)
    class InitiateDissolution {

        @Test
        @DisplayName("returns 204, moves the draft to pending and emails each signatory")
        void returns204AndNotifiesSignatories() {
            insertDissolution(DissolutionStatus.DRAFT, aDissolution());
            stubTransaction(openTransaction());
            stubOfficers(OFFICER_ONE, OFFICER_TWO);

            authorisedPost(INITIATION_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(initiationBody(Map.of("officer_id", OFFICER_ONE, "email", "new.one@doe.com"),
                            Map.of("officer_id", OFFICER_TWO, "email", "new.two@doe.com")))
                    .exchange()
                    .expectStatus().isNoContent();

            final Dissolution stored = findOnlyDissolution();
            assertThat(stored.getStatus()).isEqualTo(DissolutionStatus.PENDING);
            assertThat(stored.getData().getDirectors())
                    .extracting(DissolutionDirector::getOfficerId, DissolutionDirector::getEmail)
                    .containsExactlyInAnyOrder(
                            tuple(OFFICER_ONE, "new.one@doe.com"),
                            tuple(OFFICER_TWO, "new.two@doe.com"));
            verify(1, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)).withRequestBody(matchingJsonPath("$.email_address", equalTo("new.one@doe.com"))));
            verify(1, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)).withRequestBody(matchingJsonPath("$.email_address", equalTo("new.two@doe.com"))));
        }

        @Test
        @DisplayName("returns 400 and leaves the draft untouched when a selected director is not an officer")
        void returns400WhenSelectedDirectorIsNotAnOfficer() {
            insertDissolution(DissolutionStatus.DRAFT, aDissolution());
            stubTransaction(openTransaction());
            stubOfficers(OFFICER_ONE, OFFICER_TWO);

            authorisedPost(INITIATION_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(initiationBody(Map.of("officer_id", "unknown999", "email", "nobody@doe.com")))
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(findOnlyDissolution().getStatus()).isEqualTo(DissolutionStatus.DRAFT);
            verify(0, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)));
        }

        @Test
        @DisplayName("returns 404 when the user has no draft dissolution")
        void returns404WhenNoDraft() {
            stubTransaction(openTransaction());

            authorisedPost(INITIATION_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(initiationBody(Map.of("officer_id", OFFICER_ONE, "email", OFFICER_ONE_EMAIL)))
                    .exchange()
                    .expectStatus().isNotFound();

            verify(0, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)));
        }

        @Test
        @DisplayName("returns 409 when a pending dissolution already exists for the company")
        void returns409WhenPendingDissolutionExists() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            authorisedPost(INITIATION_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(initiationBody(Map.of("officer_id", OFFICER_ONE, "email", OFFICER_ONE_EMAIL)))
                    .exchange()
                    .expectStatus().isEqualTo(409);

            verify(0, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)));
        }

        @Test
        @DisplayName("returns 422 when no directors are provided")
        void returns422WhenNoDirectors() {
            insertDissolution(DissolutionStatus.DRAFT, aDissolution());
            stubTransaction(openTransaction());

            authorisedPost(INITIATION_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(initiationBody())
                    .exchange()
                    .expectStatus().isEqualTo(422)
                    .expectBody()
                    .jsonPath("$.directors").isEqualTo("At least 1 director must be provided");

            assertThat(findOnlyDissolution().getStatus()).isEqualTo(DissolutionStatus.DRAFT);
        }
    }

    @Nested
    @DisplayName("POST " + RESEND_NOTIFICATION_PATH)
    class ResendSignatoryNotification {

        @Test
        @DisplayName("returns 200 and re-sends the signing email to the signatory")
        void returns200AndResendsEmail() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            authorisedPost(RESEND_NOTIFICATION_PATH, OFFICER_ONE)
                    .exchange()
                    .expectStatus().isOk();

            verify(1, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)));
            verify(1, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)).withRequestBody(matchingJsonPath("$.email_address", equalTo(OFFICER_ONE_EMAIL))));
        }

        @Test
        @DisplayName("returns 400 when the user is not the applicant")
        void returns400WhenNotApplicant() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            asUser(OTHER_USER_ID, client.post(), RESEND_NOTIFICATION_PATH, OFFICER_ONE)
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(0, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)));
        }

        @Test
        @DisplayName("returns 400 when the officer is not a signatory")
        void returns400WhenSignatoryNotFound() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            authorisedPost(RESEND_NOTIFICATION_PATH, "unknown999")
                    .exchange()
                    .expectStatus().isBadRequest();

            verify(0, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)));
        }

        @Test
        @DisplayName("returns 404 when there is no pending dissolution")
        void returns404WhenNoPendingDissolution() {
            stubTransaction(openTransaction());

            authorisedPost(RESEND_NOTIFICATION_PATH, OFFICER_ONE)
                    .exchange()
                    .expectStatus().isNotFound();

            verify(0, postRequestedFor(urlEqualTo(SEND_EMAIL_URI)));
        }
    }

    @Nested
    @DisplayName("PATCH " + SIGNATORY_PATH)
    class UpdateSignatoryDetails {

        @Test
        @DisplayName("returns 200 and persists the normalised signatory details")
        void returns200AndPersistsChangedDetails() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            authorisedPatch(SIGNATORY_PATH, OFFICER_ONE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", "Jane.Doe@Mail.com", "on_behalf_name", "Jane Doe"))
                    .exchange()
                    .expectStatus().isOk();

            final DissolutionDirector updated = findSignatory(findOnlyDissolution(), OFFICER_ONE);
            assertThat(updated.getEmail()).isEqualTo("jane.doe@mail.com");
            assertThat(updated.getOnBehalfName()).isEqualTo("Jane Doe");
        }

        @Test
        @DisplayName("returns 400 and leaves details unchanged when the signatory has already approved")
        void returns400WhenSignatoryAlreadyApproved() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution().withDirectors(
                    signatory(OFFICER_ONE, OFFICER_ONE_EMAIL).withDirectorApproval(DirectorApprovalTestDataBuilder.aDirectorApproval()),
                    signatory(OFFICER_TWO, OFFICER_TWO_EMAIL)));
            stubTransaction(openTransaction());

            authorisedPatch(SIGNATORY_PATH, OFFICER_ONE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", "jane.doe@mail.com"))
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(findSignatory(findOnlyDissolution(), OFFICER_ONE).getEmail()).isEqualTo(OFFICER_ONE_EMAIL);
        }

        @Test
        @DisplayName("returns 400 and leaves details unchanged when the user is not the applicant")
        void returns400WhenNotApplicant() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            asUser(OTHER_USER_ID, client.patch(), SIGNATORY_PATH, OFFICER_ONE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", "jane.doe@mail.com"))
                    .exchange()
                    .expectStatus().isBadRequest();

            assertThat(findSignatory(findOnlyDissolution(), OFFICER_ONE).getEmail()).isEqualTo(OFFICER_ONE_EMAIL);
        }

        @Test
        @DisplayName("returns 422 when the email is invalid")
        void returns422WhenEmailInvalid() {
            insertDissolution(DissolutionStatus.PENDING, aDissolution());
            stubTransaction(openTransaction());

            authorisedPatch(SIGNATORY_PATH, OFFICER_ONE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", "not-an-email"))
                    .exchange()
                    .expectStatus().isEqualTo(422)
                    .expectBody()
                    .jsonPath("$.email").exists();

            assertThat(findSignatory(findOnlyDissolution(), OFFICER_ONE).getEmail()).isEqualTo(OFFICER_ONE_EMAIL);
        }
    }

    private static Transaction openTransaction() {
        return transaction(TransactionStatus.OPEN);
    }

    private static Transaction transaction(TransactionStatus status) {
        return TransactionTestDataBuilder.aTransaction()
                .withStatus(status)
                .withCompanyNumber(COMPANY_NUMBER)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_DS01, COMPANY_NUMBER))
                .build();
    }

    private void stubTransaction(Transaction transaction) {
        stubFor(get(TRANSACTION_URI).willReturn(okJson(toJson(transaction))));
    }

    private void stubCompanyProfile(CompanyProfileApi companyProfile) {
        stubFor(get(COMPANY_PROFILE_URI).willReturn(okJson(toJson(companyProfile))));
    }

    private void stubOfficers(String... officerIds) {
        final List<CompanyOfficer> officers = Arrays.stream(officerIds).map(officerId -> {
            final CompanyOfficer officer = CompanyOfficerFixtures.generateCompanyOfficer();
            officer.setLinks(CompanyOfficerFixtures.generateCompanyOfficerLinks(officerId));
            return officer;
        }).toList();
        final CompanyOfficersResponse response = new CompanyOfficersResponse();
        response.setItems(officers);
        stubFor(get(urlPathEqualTo(OFFICERS_URI)).willReturn(okJson(toJson(response))));
    }

    private static DissolutionTestDataBuilder aDissolution() {
        return DissolutionTestDataBuilder.aDissolution()
                .withCompanyNumber(COMPANY_NUMBER)
                .withTransactionId(TRANSACTION_ID)
                .withCreatedBy(CreatedByTestDataBuilder.aCreatedBy().withUserId(ERIC_IDENTITY_VALUE))
                .withDirectors(signatory(OFFICER_ONE, OFFICER_ONE_EMAIL), signatory(OFFICER_TWO, OFFICER_TWO_EMAIL))
                .withActive(false);
    }

    private static DissolutionDirectorTestDataBuilder signatory(String officerId, String email) {
        return DissolutionDirectorTestDataBuilder.aDissolutionDirector().withOfficerId(officerId).withEmail(email);
    }

    private Dissolution insertDissolution(DissolutionStatus status, DissolutionTestDataBuilder builder) {
        return mongoTemplate.insert(builder.withStatus(status).build());
    }

    private Dissolution findOnlyDissolution() {
        final List<Dissolution> stored = mongoTemplate.findAll(Dissolution.class);
        assertThat(stored).hasSize(1);
        return stored.getFirst();
    }

    private static DissolutionDirector findSignatory(Dissolution dissolution, String officerId) {
        return dissolution.findSignatory(officerId).orElseThrow();
    }

    private static Map<String, Object> approvalBody(String officerId, boolean hasApproved) {
        return Map.of("officer_id", officerId, "has_approved", hasApproved, "ip_address", "127.0.0.1");
    }

    @SafeVarargs
    private static Map<String, Object> initiationBody(Map<String, String>... directors) {
        return Map.of("directors", List.of(directors));
    }

    private RestTestClient.RequestBodySpec authorisedPost(String path, Object... extraUriVars) {
        return asUser(ERIC_IDENTITY_VALUE, client.post(), path, extraUriVars);
    }

    private RestTestClient.RequestBodySpec authorisedPatch(String path, Object... extraUriVars) {
        return asUser(ERIC_IDENTITY_VALUE, client.patch(), path, extraUriVars);
    }

    private static RestTestClient.RequestBodySpec asUser(String userId, RestTestClient.RequestBodyUriSpec request,
                                                         String path, Object... extraUriVars) {
        return request.uri(path, uriVars(extraUriVars))
                .headers(h -> h.addAll(ericUserHeaders(userId, COMPANY_NUMBER)));
    }

    private static Object[] uriVars(Object... extraUriVars) {
        return Stream.concat(Stream.of(COMPANY_NUMBER, TRANSACTION_ID), Arrays.stream(extraUriVars)).toArray();
    }

    /**
     * Headers for a signed-in user authorised to dissolve the given company, as provided by the ERIC gateway.
     */
    private static HttpHeaders ericUserHeaders(String userId, String companyNumber) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(EricConstants.ERIC_IDENTITY, userId);
        headers.add("ERIC-Authorised-User", ERIC_EMAIL);
        headers.add(HEADER_ERIC_REQUEST_ID, ERIC_REQUEST_ID);
        headers.add(EricConstants.ERIC_AUTHORISED_TOKEN_PERMISSIONS, String.format(
                "%s=%s %s=%s",
                Permission.Key.COMPANY_NUMBER, companyNumber,
                Permission.Key.COMPANY_STATUS, Permission.Value.UPDATE
        ));
        headers.add("ERIC-Access-Token", ERIC_ACCESS_TOKEN);
        return headers;
    }
}
