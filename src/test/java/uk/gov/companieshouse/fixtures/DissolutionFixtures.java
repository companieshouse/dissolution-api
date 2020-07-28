package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.db.dissolution.*;
import uk.gov.companieshouse.model.dto.dissolution.*;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.ApplicationType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DissolutionFixtures {

    public static DissolutionPatchRequest generateDissolutionPatchRequest() {
        final DissolutionPatchRequest request = new DissolutionPatchRequest();

        request.setEmail("user@mail.com");
        request.setHasApproved(true);

        return request;
    }

    public static DissolutionCreateRequest generateDissolutionCreateRequest() {
        final DissolutionCreateRequest request = new DissolutionCreateRequest();

        request.setDirectors(Collections.singletonList(generateDirectorRequest()));

        return request;
    }

    public static DirectorRequest generateDirectorRequest() {
        final DirectorRequest director = new DirectorRequest();

        director.setName("John Doe");
        director.setEmail("user@mail.com");

        return director;
    }

    public static DissolutionPatchResponse generateDissolutionPatchResponse() {
        return new DissolutionPatchResponse();
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

        director.setName("John Doe");
        director.setEmail("john@doe.com");

        return director;
    }

    public static List<DissolutionDirector> generateDissolutionDirectorList() {
        final List<DissolutionDirector> directorList = new ArrayList<>();
        final DissolutionDirector director = new DissolutionDirector();

        director.setName("John Doe");
        director.setEmail("john@doe.com");

        directorList.add(director);

        final DissolutionDirector directorTwo = new DissolutionDirector();

        directorTwo.setName("Fred Mercure");
        directorTwo.setEmail("fred@mercure.com");

        directorList.add(directorTwo);

        return directorList;
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
}
