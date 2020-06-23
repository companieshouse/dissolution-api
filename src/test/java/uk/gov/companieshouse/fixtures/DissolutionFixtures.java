package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.db.*;
import uk.gov.companieshouse.model.dto.CreateDissolutionRequestDTO;
import uk.gov.companieshouse.model.dto.CreateDissolutionResponseDTO;
import uk.gov.companieshouse.model.dto.DirectorRequestDTO;

import java.time.LocalDateTime;
import java.util.Collections;

public class DissolutionFixtures {

    public static CreateDissolutionRequestDTO generateCreateDissolutionRequestDTO() {
        return new CreateDissolutionRequestDTO() {{
            setDirectors(Collections.singletonList(generateDirectorRequestDTO()));
        }};
    }

    public static DirectorRequestDTO generateDirectorRequestDTO() {
        return new DirectorRequestDTO() {{
           setName("John Doe");
           setEmail("user@mail.com");
        }};
    }

    public static CreateDissolutionResponseDTO generateCreateDissolutionResponseDTO() {
        return new CreateDissolutionResponseDTO();
    }

    public static Dissolution generateDissolution() {
        return new Dissolution() {{
            setModifiedDateTime(LocalDateTime.now());
            setData(generateDissolutionData());
            setCompany(generateCompany());
            setCreatedBy(generateCreatedBy());
        }};
    }

    public static DissolutionData generateDissolutionData() {
        return new DissolutionData() {{
            setETag("someETag");
            setApplication(generateDissolutionApplication());
            setDirectors(Collections.singletonList(generateDissolutionDirector()));
        }};
    }

    public static DissolutionApplication generateDissolutionApplication() {
        return new DissolutionApplication() {{
            setReference("ABC123");
            setType(DissolutionType.DS01);
            setStatus(DissolutionStatus.PENDING_APPROVAL);
        }};
    }

    public static DissolutionDirector generateDissolutionDirector() {
        return new DissolutionDirector() {{
            setName("John Doe");
            setEmail("john@doe.com");
        }};
    }

    public static Company generateCompany() {
        return new Company() {{
            setNumber("12345678");
            setName("Companies House");
        }};
    }

    public static CreatedBy generateCreatedBy() {
        return new CreatedBy() {{
            setUserId("user123");
            setIpAddress("192.168.0.2");
            setEmail("user@mail.com");
            setDateTime(LocalDateTime.now());
        }};
    }
}
