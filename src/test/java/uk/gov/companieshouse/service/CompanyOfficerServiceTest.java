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

        CompanyOfficer activeCorporateDirector = generateCompanyOfficer();
        activeCorporateDirector.setResignedOn(null);
        activeCorporateDirector.setOfficerRole(OfficerRole.CORPORATE_DIRECTOR.getValue());
        activeCorporateDirector.setLinks(generateCompanyOfficerLinks("321abc"));

        CompanyOfficer activeCorporateNomineeDirector = generateCompanyOfficer();
        activeCorporateNomineeDirector.setResignedOn(null);
        activeCorporateNomineeDirector.setOfficerRole(OfficerRole.CORPORATE_NOMINEE_DIRECTOR.getValue());
        activeCorporateNomineeDirector.setLinks(generateCompanyOfficerLinks("321cba"));

        CompanyOfficer activeJudicialFactor = generateCompanyOfficer();
        activeJudicialFactor.setResignedOn(null);
        activeJudicialFactor.setOfficerRole(OfficerRole.JUDICIAL_FACTOR.getValue());
        activeJudicialFactor.setLinks(generateCompanyOfficerLinks("abc123"));

        CompanyOfficer activeSecretary = generateCompanyOfficer();
        activeSecretary.setResignedOn(null);
        activeSecretary.setOfficerRole(OfficerRole.SECRETARY.getValue());
        activeSecretary.setLinks(generateCompanyOfficerLinks("456def"));

        CompanyOfficer activeMember = generateCompanyOfficer();
        activeMember.setResignedOn(null);
        activeMember.setOfficerRole(OfficerRole.LLP_MEMBER.getValue());
        activeMember.setLinks(generateCompanyOfficerLinks("789ghi"));

        CompanyOfficer activeDesignatedMember = generateCompanyOfficer();
        activeDesignatedMember.setResignedOn(null);
        activeDesignatedMember.setOfficerRole(OfficerRole.LLP_DESIGNATED_MEMBER.getValue());
        activeDesignatedMember.setLinks(generateCompanyOfficerLinks("987ghi"));

        CompanyOfficer activeCorporateMember = generateCompanyOfficer();
        activeCorporateMember.setResignedOn(null);
        activeCorporateMember.setOfficerRole(OfficerRole.CORPORATE_LLP_MEMBER.getValue());
        activeCorporateMember.setLinks(generateCompanyOfficerLinks("987ihg"));

        CompanyOfficer activeCorporateDesignatedMember = generateCompanyOfficer();
        activeCorporateDesignatedMember.setResignedOn(null);
        activeCorporateDesignatedMember.setOfficerRole(OfficerRole.CORPORATE_LLP_DESIGNATED_MEMBER.getValue());
        activeCorporateDesignatedMember.setLinks(generateCompanyOfficerLinks("ghi789"));

        when(client.getCompanyOfficers(COMPANY_NUMBER)).thenReturn(Arrays.asList(
                activeDirector,
                activeCorporateDirector,
                activeCorporateNomineeDirector,
                activeJudicialFactor,
                activeSecretary,
                activeMember,
                activeDesignatedMember,
                activeCorporateMember,
                activeCorporateDesignatedMember
        ));

        final Map<String, CompanyOfficer> result = service.getActiveDirectorsForCompany(COMPANY_NUMBER);

        assertEquals(8, result.size());
        assertEquals(activeDirector, result.get("123abc"));
        assertEquals(activeCorporateDirector, result.get("321abc"));
        assertEquals(activeCorporateNomineeDirector, result.get("321cba"));
        assertEquals(activeJudicialFactor, result.get("abc123"));
        assertNull(result.get("456def"));
        assertEquals(activeMember, result.get("789ghi"));
        assertEquals(activeDesignatedMember, result.get("987ghi"));
        assertEquals(activeCorporateMember, result.get("987ihg"));
        assertEquals(activeCorporateDesignatedMember, result.get("ghi789"));
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
