package uk.gov.companieshouse.mapper.filing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import uk.gov.companieshouse.exception.FilingDataMapperException;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;

@ExtendWith(MockitoExtension.class)
class FilingDataMapperTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DISSOLUTION_ID = "12345678";
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String PAYMENT_METHOD = "credit-card";
    public static final String DEFAULT_FORENAME = "John";
    public static final String DEFAULT_SURNAME = "DOE";
    private static final String ON_BEHALF_NAME = "on behalf name";

    private ObjectMapper objectMapper;

    private FilingDataMapper mapper;

    private Dissolution dissolution;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        mapper = new FilingDataMapper(objectMapper);
        dissolution = aDissolution().withId(DISSOLUTION_ID).build();

        final DirectorApproval approvalOne = DissolutionFixtures.generateDirectorApproval();
        approvalOne.setDateTime(LocalDateTime.of(2020, 10, 20, 0, 0));

        final DissolutionDirector directorOne = DissolutionFixtures.generateDissolutionDirector();
        directorOne.setName(DEFAULT_SURNAME + ", " + DEFAULT_FORENAME);
        directorOne.setOnBehalfName(null);
        directorOne.setDirectorApproval(approvalOne);

        dissolution.getData().setDirectors(List.of(directorOne));
        dissolution.setCertificate(DissolutionFixtures.generateDissolutionCertificate());
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapToFilingData_setsTheFilingDataCorrectly() {
        var expectedCompany = dissolution.getCompany();
        var expectedSignDate = dissolution.getCreatedBy().getDateTime().format(DATE_FORMATTER);
        var expectedDissolutionDirector = dissolution.getData().getDirectors().getFirst();
        var expectedDirectorSignDate = expectedDissolutionDirector.getDirectorApproval().getDateTime().format(DATE_FORMATTER);
        var expectedAttachmentUri = String.format("s3://%s/%s", dissolution.getCertificate().getBucket(), dissolution.getCertificate().getKey());

        final Map<String, Object> result = mapper.mapToFilingData(dissolution, PAYMENT_REFERENCE, PAYMENT_METHOD);

        assertEquals(expectedCompany.getName(), result.get("company_name"));
        assertEquals(expectedCompany.getNumber(), result.get("company_number"));
        assertEquals(PAYMENT_REFERENCE, result.get("payment_reference"));
        assertEquals(PAYMENT_METHOD, result.get("payment_method"));

        assertEquals(expectedSignDate, result.get("sign_date"));

        List<Map<String, Object>> officers = (List<Map<String, Object>>) result.get("officers");
        assertEquals(1, officers.size());

        Map<String, Object> officerMap = officers.getFirst();
        assertEquals(expectedDissolutionDirector.getEmail(), officerMap.get("email"));
        assertEquals(expectedDissolutionDirector.getDirectorApproval().getIpAddress(), officerMap.get("ip_address"));
        assertEquals(expectedDirectorSignDate, officerMap.get("sign_date"));
        assertFalse(officerMap.containsKey("on_behalf_name"));

        Map<String, Object> personNameMap = (Map<String, Object>) officerMap.get("person_name");
        assertEquals(DEFAULT_FORENAME, personNameMap.get("forename"));
        assertEquals(DEFAULT_SURNAME, personNameMap.get("surname"));

        List<Map<String, Object>> links = (List<Map<String, Object>>) result.get("links");
        assertEquals(1, links.size());
        Map<String, Object> linkMap = links.getFirst();
        assertEquals("dissolution", linkMap.get("rel"));
        assertEquals(expectedAttachmentUri, linkMap.get("href"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapToFilingData_includesOnBehalfName_whenDirectorIsSigningOnBehalf() {
        dissolution.getData().getDirectors().get(0).setOnBehalfName(ON_BEHALF_NAME);

        final Map<String, Object> result = mapper.mapToFilingData(dissolution, PAYMENT_REFERENCE, PAYMENT_METHOD);

        List<Map<String, Object>> officers = (List<Map<String, Object>>) result.get("officers");
        assertEquals(1, officers.size());

        Map<String, Object> officerMap = officers.getFirst();
        assertEquals(ON_BEHALF_NAME, officerMap.get("on_behalf_name"));

        Map<String, Object> personNameMap = (Map<String, Object>) officerMap.get("person_name");
        assertEquals(DEFAULT_FORENAME, personNameMap.get("forename"));
        assertEquals(DEFAULT_SURNAME, personNameMap.get("surname"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapToFilingData_mapsAllDirectors_whenMultipleDirectors() {
        var approvalTwo = DissolutionFixtures.generateDirectorApproval();
        approvalTwo.setDateTime(LocalDateTime.of(2021, 5, 10, 0, 0));

        var expectedForename = "Jane";
        var expectedSurname = "SMITH";
        var directorTwo = DissolutionFixtures.generateDissolutionDirector();
        directorTwo.setName(expectedSurname + ", " + expectedForename);
        directorTwo.setEmail("jane@smith.com");
        directorTwo.setOnBehalfName(null);
        directorTwo.setDirectorApproval(approvalTwo);

        dissolution.getData().setDirectors(List.of(dissolution.getData().getDirectors().get(0), directorTwo));

        var expectedDissolutionDirector = dissolution.getData().getDirectors().getLast();
        var expectedSignDate = expectedDissolutionDirector.getDirectorApproval().getDateTime().format(DATE_FORMATTER);

        final Map<String, Object> result = mapper.mapToFilingData(dissolution, PAYMENT_REFERENCE, PAYMENT_METHOD);

        List<Map<String, Object>> officers = (List<Map<String, Object>>) result.get("officers");
        assertEquals(2, officers.size());

        Map<String, Object> officerMap = officers.getLast();
        assertEquals(expectedDissolutionDirector.getEmail(), officerMap.get("email"));
        assertEquals(expectedDissolutionDirector.getDirectorApproval().getIpAddress(), officerMap.get("ip_address"));
        assertEquals(expectedSignDate, officerMap.get("sign_date"));
        assertFalse(officerMap.containsKey("on_behalf_name"));

        Map<String, Object> personNameMap = (Map<String, Object>) officerMap.get("person_name");
        assertEquals(expectedForename, personNameMap.get("forename"));
        assertEquals(expectedSurname, personNameMap.get("surname"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapToFilingData_handlesMissingForename_whenNameContainsOnlySurname() {
        var expectedDissolutionDirector = dissolution.getData().getDirectors().getFirst();
        expectedDissolutionDirector.setName(DEFAULT_SURNAME);

        final Map<String, Object> result = mapper.mapToFilingData(dissolution, PAYMENT_REFERENCE, PAYMENT_METHOD);

        List<Map<String, Object>> officers = (List<Map<String, Object>>) result.get("officers");
        assertEquals(1, officers.size());

        Map<String, Object> officerMap = officers.getFirst();
        Map<String, Object> personNameMap = (Map<String, Object>) officerMap.get("person_name");
        assertFalse(personNameMap.containsKey("forename"));
        assertEquals(DEFAULT_SURNAME, personNameMap.get("surname"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapToFilingData_throwsFilingDataMapperException_whenConversionFails() {
        String expected = "Failed to map to filing data for company " + dissolution.getCompany().getNumber() + " with dissolution " + dissolution.getId();

        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.convertValue(any(), any(TypeReference.class))).thenThrow(JacksonException.class);

        mapper = new FilingDataMapper(mockObjectMapper);

        FilingDataMapperException ex = assertThrows(
                FilingDataMapperException.class,
                () -> mapper.mapToFilingData(dissolution, PAYMENT_REFERENCE, PAYMENT_METHOD)
        );
        assertEquals(expected, ex.getMessage());
    }
}
