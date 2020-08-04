package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.CompanyOfficersClient;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;

@ExtendWith(MockitoExtension.class)
public class CompanyOfficerServiceTest {

    @InjectMocks
    private CompanyOfficerService companyOfficerService;

    @Mock
    private CompanyOfficerValidator companyOfficerValidator;

    @Mock
    private CompanyOfficersClient companyOfficersClient;

    public static final String COMPANY_NUMBER = "12345678";

    @Test
    public void hasEnoughOfficersSelected_areMajorityOfCompanyOfficersSelected_returnsTrue() {
        final List<CompanyOfficer> officers = Collections.singletonList(generateCompanyOfficer());
        final DissolutionCreateRequest dissolutionRequest = DissolutionFixtures.generateDissolutionCreateRequest();

        when(companyOfficerValidator.areMajorityOfCompanyOfficersSelected(officers, dissolutionRequest.getDirectors())).thenReturn(true);
        when(companyOfficersClient.getCompanyOfficers(COMPANY_NUMBER)).thenReturn(officers);

        final boolean result = companyOfficerService.hasEnoughOfficersSelected(COMPANY_NUMBER, dissolutionRequest.getDirectors());

        verify(companyOfficerValidator).areMajorityOfCompanyOfficersSelected(officers, dissolutionRequest.getDirectors());

        assertTrue(result);
    }

    @Test
    public void hasEnoughOfficersSelected_areMajorityOfCompanyOfficersSelected_returnsFalse() {
        final List<CompanyOfficer> officers = Collections.singletonList(generateCompanyOfficer());
        final DissolutionCreateRequest dissolutionRequest = DissolutionFixtures.generateDissolutionCreateRequest();


        when(companyOfficerValidator.areMajorityOfCompanyOfficersSelected(officers, dissolutionRequest.getDirectors())).thenReturn(false);
        when(companyOfficersClient.getCompanyOfficers(COMPANY_NUMBER)).thenReturn(officers);

        final boolean result = companyOfficerService.hasEnoughOfficersSelected(COMPANY_NUMBER, dissolutionRequest.getDirectors());

        verify(companyOfficerValidator).areMajorityOfCompanyOfficersSelected(officers, dissolutionRequest.getDirectors());

        assertFalse(result);
    }
}
