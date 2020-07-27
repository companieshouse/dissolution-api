package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.CompanyOfficersClient;
import uk.gov.companieshouse.api.model.officers.CompanyOfficerApi;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.fixtures.CompanyOfficerFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CompanyOfficerServiceTest {

    @InjectMocks
    private CompanyOfficerService companyOfficerService;

    @Mock
    private CompanyOfficerValidator companyOfficerValidator;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private CompanyOfficersClient companyOfficersClient;

    public static final String COMPANY_NUMBER = "12345678";

    @Test
    public void hasEnoughOfficersSelected_areMajorityOfCompanyOfficersSelected_returnsTrue() {
        final List<CompanyOfficerApi> officers = CompanyOfficerFixtures.generateCompanyOfficerList();
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        when(companyOfficerValidator.areMajorityOfCompanyOfficersSelected(officers, dissolution.getData().getDirectors())).thenReturn(true);
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(java.util.Optional.of(dissolution));
        when(companyOfficersClient.getCompanyOfficers(COMPANY_NUMBER)).thenReturn(officers);

        final boolean result = companyOfficerService.hasEnoughOfficersSelected(COMPANY_NUMBER);

        verify(companyOfficerValidator).areMajorityOfCompanyOfficersSelected(officers, dissolution.getData().getDirectors());

        assertTrue(result);
    }

    @Test
    public void hasEnoughOfficersSelected_areMajorityOfCompanyOfficersSelected_returnsFalse() {
        final List<CompanyOfficerApi> officers = CompanyOfficerFixtures.generateCompanyOfficerList();
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();

        when(companyOfficerValidator.areMajorityOfCompanyOfficersSelected(officers, dissolution.getData().getDirectors())).thenReturn(false);
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(java.util.Optional.of(dissolution));
        when(companyOfficersClient.getCompanyOfficers(COMPANY_NUMBER)).thenReturn(officers);

        final boolean result = companyOfficerService.hasEnoughOfficersSelected(COMPANY_NUMBER);

        verify(companyOfficerValidator).areMajorityOfCompanyOfficersSelected(officers, dissolution.getData().getDirectors());

        assertFalse(result);
    }
}
