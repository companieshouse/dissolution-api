package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.db.dissolution.Company;
import uk.gov.companieshouse.model.db.dissolution.CreatedBy;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionApplication;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.db.dissolution.DissolutionData;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.dissolution.DissolutionRejectReason;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.domain.DissolutionUserData;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.documentrender.DissolutionCertificateData;
import uk.gov.companieshouse.model.dto.documentrender.DissolutionCertificateDirector;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.SubmissionStatus;
import uk.gov.companieshouse.model.enums.VerdictResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DissolutionFixtures {

    public static DissolutionPatchRequest generateDissolutionPatchRequest() {
        final DissolutionPatchRequest request = new DissolutionPatchRequest();

        request.setOfficerId("abc123");
        request.setHasApproved(true);
        request.setIpAddress("127.0.0.1");

        return request;
    }

    public static DissolutionCreateRequest generateDissolutionCreateRequest() {
        final DissolutionCreateRequest request = new DissolutionCreateRequest();

        request.setDirectors(Collections.singletonList(generateDirectorRequest()));

        return request;
    }

    public static DirectorRequest generateDirectorRequest() {
        final DirectorRequest director = new DirectorRequest();

        director.setOfficerId("abc123");
        director.setEmail("user@mail.com");

        return director;
    }

    public static DissolutionPatchResponse generateDissolutionPatchResponse() {
        return new DissolutionPatchResponse();
    }

    public static DissolutionDirectorPatchResponse generateDissolutionDirectorPatchResponse() {
        return new DissolutionDirectorPatchResponse();
    }

    public static DissolutionCreateResponse generateDissolutionCreateResponse() {
        return new DissolutionCreateResponse();
    }

    public static DissolutionGetResponse generateDissolutionGetResponse() {
        return new DissolutionGetResponse();
    }

    public static Dissolution generateDissolution() {
        final Dissolution dissolution = new Dissolution();

        dissolution.setModifiedDateTime(LocalDateTime.now());
        dissolution.setData(generateDissolutionData());
        dissolution.setCompany(generateCompany());
        dissolution.setCreatedBy(generateCreatedBy());
        dissolution.setSubmission(new DissolutionSubmission());
        dissolution.setPaymentInformation(new PaymentInformation());
        dissolution.setVerdict(new DissolutionVerdict());
        dissolution.setActive(true);

        return dissolution;
    }

    public static DissolutionData generateDissolutionData() {
        final DissolutionData data = new DissolutionData();

        data.setETag("someETag");
        data.setApplication(generateDissolutionApplication());
        data.setDirectors(Collections.singletonList(generateDissolutionDirector()));

        return data;
    }

    public static DissolutionApplication generateDissolutionApplication() {
        final DissolutionApplication application = new DissolutionApplication();

        application.setReference("ABC123");
        application.setType(ApplicationType.DS01);
        application.setStatus(ApplicationStatus.PENDING_APPROVAL);

        return application;
    }

    public static DissolutionDirector generateDissolutionDirector() {
        final DissolutionDirector director = new DissolutionDirector();

        director.setOfficerId("abc123");
        director.setName("DOE, John James");
        director.setEmail("john@doe.com");

        return director;
    }

    public static List<DissolutionDirector> generateDissolutionDirectorList() {
        final DissolutionDirector director = new DissolutionDirector();

        director.setOfficerId("abc123");
        director.setName("DOE, John James");
        director.setEmail("john@doe.com");

        final DissolutionDirector directorTwo = new DissolutionDirector();

        directorTwo.setOfficerId("def456");
        directorTwo.setName("MERCURE, Fred");
        directorTwo.setEmail("fred@mercure.com");

        return Arrays.asList(director, directorTwo);
    }

    public static Company generateCompany() {
        final Company company = new Company();

        company.setNumber("12345678");
        company.setName("Companies House");

        return company;
    }

    public static CreatedBy generateCreatedBy() {
        final CreatedBy createdBy = new CreatedBy();

        createdBy.setUserId("user123");
        createdBy.setIpAddress("192.168.0.2");
        createdBy.setEmail("user@mail.com");
        createdBy.setDateTime(LocalDateTime.now());

        return createdBy;
    }

    public static DirectorApproval generateDirectorApproval() {
        final DirectorApproval approval = new DirectorApproval();

        approval.setUserId("user123");
        approval.setIpAddress("192.168.0.2");
        approval.setDateTime(LocalDateTime.now());

        return approval;
    }

    public static DissolutionCertificate generateDissolutionCertificate() {
        final DissolutionCertificate certificate = new DissolutionCertificate();

        certificate.setBucket("some-bucket");
        certificate.setKey("some-key");

        return certificate;
    }

    public static DissolutionCertificateData generateDissolutionCertificateData() {
        final DissolutionCertificateData data = new DissolutionCertificateData();

        data.setCompanyName("Some company");
        data.setCompanyNumber("1234");
        data.setDirectors(Collections.singletonList(generateDissolutionCertificateDirector()));

        return data;
    }

    public static DissolutionCertificateDirector generateDissolutionCertificateDirector() {
        final DissolutionCertificateDirector director = new DissolutionCertificateDirector();

        director.setName("Mr Director");
        director.setApprovalDate("2020-01-01");

        return director;
    }

    public static DissolutionSubmission generateDissolutionSubmission() {
        final DissolutionSubmission submission = new DissolutionSubmission();

        submission.setStatus(SubmissionStatus.PENDING);
        submission.setRetryCounter(0);

        return submission;
    }

    public static DissolutionVerdict generateDissolutionVerdict() {
        final DissolutionVerdict dissolutionVerdict = new DissolutionVerdict();

        dissolutionVerdict.setResult(VerdictResult.ACCEPTED);
        dissolutionVerdict.setDateTime(LocalDateTime.now());

        return dissolutionVerdict;
    }

    public static DissolutionRejectReason generateDissolutionRejectReason() {
        final DissolutionRejectReason dissolutionRejectReason = new DissolutionRejectReason();

        dissolutionRejectReason.setId("1");
        dissolutionRejectReason.setDescription("some description");
        dissolutionRejectReason.setTextEnglish("some reject reason");

        return dissolutionRejectReason;
    }

    public static DissolutionUserData generateDissolutionUserData() {
        final DissolutionUserData userData = new DissolutionUserData();

        userData.setEmail("some@email.com");
        userData.setIpAddress("some ipAddress");
        userData.setUserId("some user id");

        return userData;
    }
}
