package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.db.*;
import uk.gov.companieshouse.model.dto.CreateDissolutionRequestDTO;
import uk.gov.companieshouse.model.dto.CreateDissolutionResponseDTO;
import uk.gov.companieshouse.model.dto.DirectorRequestDTO;

import java.time.LocalDateTime;
import java.util.Collections;

public class DissolutionFixtures {

    public static CreateDissolutionRequestDTO generateCreateDissolutionRequestDTO() {
        final CreateDissolutionRequestDTO request = new CreateDissolutionRequestDTO();

        request.setDirectors(Collections.singletonList(generateDirectorRequestDTO()));

        return request;
    }

    public static DirectorRequestDTO generateDirectorRequestDTO() {
        final DirectorRequestDTO director = new DirectorRequestDTO();

        director.setName("John Doe");
        director.setEmail("user@mail.com");

        return director;
    }

    public static CreateDissolutionResponseDTO generateCreateDissolutionResponseDTO() {
        return new CreateDissolutionResponseDTO();
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
        application.setType(DissolutionApplication.DissolutionType.DS01);
        application.setStatus(DissolutionApplication.DissolutionStatus.PENDING_APPROVAL);

        return application;
    }

    public static DissolutionDirector generateDissolutionDirector() {
        final DissolutionDirector director = new DissolutionDirector();

        director.setName("John Doe");
        director.setEmail("john@doe.com");

        return director;
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
}
