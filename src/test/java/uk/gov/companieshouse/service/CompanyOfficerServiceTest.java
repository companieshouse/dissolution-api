package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.CompanyOfficersClient;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.enums.OfficerRole;
import uk.gov.companieshouse.service.dissolution.validator.CompanyOfficerValidator;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficerLinks;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorRequest;

@ExtendWith(MockitoExtension.class)
public class CompanyOfficerServiceTest {

    @InjectMocks
    private CompanyOfficerService service;

    @Mock
    private CompanyOfficersClient client;

    @Mock
    private CompanyOfficerValidator validator;

    public static final String COMPANY_NUMBER = "12345678";

    @Test
    public void getActiveDirectorsForCompany_shouldFetchOfficers_AndIndexUsingOfficerId() {
        CompanyOfficer activeDirector1 = generateCompanyOfficer();
        activeDirector1.setResignedOn(null);
        activeDirector1.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        activeDirector1.setLinks(generateCompanyOfficerLinks("123abc"));

        CompanyOfficer activeDirector2 = generateCompanyOfficer();
        activeDirector2.setResignedOn(null);
        activeDirector2.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        activeDirector2.setLinks(generateCompanyOfficerLinks("456def"));

        when(client.getCompanyOfficers(COMPANY_NUMBER)).thenReturn(Arrays.asList(activeDirector1, activeDirector2));

        final Map<String, CompanyOfficer> result = service.getActiveDirectorsForCompany(COMPANY_NUMBER);

        verify(client).getCompanyOfficers(COMPANY_NUMBER);

        assertEquals(2, result.size());
        assertEquals(activeDirector1, result.get("123abc"));
        assertEquals(activeDirector2, result.get("456def"));
    }

    @Test
    public void getActiveDirectorsForCompany_shouldFetchOfficers_filterOutResignedOfficers() {
        CompanyOfficer activeDirector1 = generateCompanyOfficer();
        activeDirector1.setResignedOn(null);
        activeDirector1.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        activeDirector1.setLinks(generateCompanyOfficerLinks("123abc"));

        CompanyOfficer activeDirector2 = generateCompanyOfficer();
        activeDirector2.setResignedOn(null);
        activeDirector2.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        activeDirector2.setLinks(generateCompanyOfficerLinks("456def"));

        CompanyOfficer resignedDirector = generateCompanyOfficer();
        resignedDirector.setResignedOn(LocalDateTime.now().toString());
        resignedDirector.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        resignedDirector.setLinks(generateCompanyOfficerLinks("789ghi"));

        when(client.getCompanyOfficers(COMPANY_NUMBER)).thenReturn(Arrays.asList(activeDirector1, activeDirector2, resignedDirector));

        final Map<String, CompanyOfficer> result = service.getActiveDirectorsForCompany(COMPANY_NUMBER);

        assertEquals(2, result.size());
        assertEquals(activeDirector1, result.get("123abc"));
        assertEquals(activeDirector2, result.get("456def"));
        assertNull(result.get("789ghi"));
    }

    @Test
    public void getActiveDirectorsForCompany_shouldFetchOfficers_filterOutNonDirectorsAndNonMembers() {
        CompanyOfficer activeDirector = generateCompanyOfficer();
        activeDirector.setResignedOn(null);
        activeDirector.setOfficerRole(OfficerRole.DIRECTOR.getValue());
        activeDirector.setLinks(generateCompanyOfficerLinks("123abc"));

        CompanyOfficer activeSecretary = generateCompanyOfficer();
        activeSecretary.setResignedOn(null);
        activeSecretary.setOfficerRole(OfficerRole.SECRETARY.getValue());
        activeSecretary.setLinks(generateCompanyOfficerLinks("456def"));

        CompanyOfficer activeMember = generateCompanyOfficer();
        activeMember.setResignedOn(null);
        activeMember.setOfficerRole(OfficerRole.LLP_MEMBER.getValue());
        activeMember.setLinks(generateCompanyOfficerLinks("789ghi"));

        when(client.getCompanyOfficers(COMPANY_NUMBER)).thenReturn(Arrays.asList(activeDirector, activeSecretary, activeMember));

        final Map<String, CompanyOfficer> result = service.getActiveDirectorsForCompany(COMPANY_NUMBER);

        assertEquals(2, result.size());
        assertEquals(activeDirector, result.get("123abc"));
        assertNull(result.get("456def"));
        assertEquals(activeMember, result.get("789ghi"));
    }

    @Test
    public void areSelectedDirectorsValid_shouldReturnError_ifSelectedDirectorsAreNotValid() {
        final String error = "Some invalid director error";

        final Map<String, CompanyOfficer> companyDirectors = Map.of("123abc", generateCompanyOfficer());
        final List<DirectorRequest> selectedDirectors = Collections.singletonList(generateDirectorRequest());

        when(validator.areSelectedDirectorsValid(companyDirectors, selectedDirectors)).thenReturn(Optional.of(error));

        final Optional<String> result = service.areSelectedDirectorsValid(companyDirectors, selectedDirectors);

        assertEquals(error, result.get());
    }

    @Test
    public void areSelectedDirectorsValid_shouldReturnNoError_ifSelectedDirectorsAreValid() {
        final Map<String, CompanyOfficer> companyDirectors = Map.of("123abc", generateCompanyOfficer());
        final List<DirectorRequest> selectedDirectors = Collections.singletonList(generateDirectorRequest());

        when(validator.areSelectedDirectorsValid(companyDirectors, selectedDirectors)).thenReturn(Optional.empty());

        final Optional<String> result = service.areSelectedDirectorsValid(companyDirectors, selectedDirectors);

        assertTrue(result.isEmpty());
    }
}
