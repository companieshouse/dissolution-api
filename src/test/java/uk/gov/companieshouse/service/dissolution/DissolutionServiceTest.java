package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;
import static uk.gov.companieshouse.fixtures.PaymentFixtures.generatePaymentPatchRequest;

@ExtendWith(MockitoExtension.class)
public class DissolutionServiceTest {

    @InjectMocks
    private DissolutionService service;

    @Mock
    private DissolutionCreator creator;

    @Mock
    private DissolutionGetter getter;

    @Mock
    private DissolutionPatcher patcher;

    @Mock
    private DissolutionRepository repository;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String APPLICATION_REFERENCE = "XYZ456";
    public static final String USER_ID = "123";
    public static final String IP = "192.168.0.1";
    public static final String EMAIL = "user@mail.com";
    public static final String OFFICER_ID = "abc123";

    @Test
    public void create_createsADissolutionRequest_returnsCreateResponse() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final DissolutionCreateResponse response = DissolutionFixtures.generateDissolutionCreateResponse();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        final Map<String, CompanyOfficer> companyDirectors = Map.of(OFFICER_ID, generateCompanyOfficer());

        when(creator.create(body, company, companyDirectors, USER_ID, IP, EMAIL)).thenReturn(response);

        final DissolutionCreateResponse result = service.create(body, company, companyDirectors, USER_ID, IP, EMAIL);

        verify(creator).create(body, company, companyDirectors, USER_ID, IP, EMAIL);

        assertEquals(response, result);
    }

    @Test
    public void doesDissolutionRequestExistForCompanyByCompanyNumber_returnsTrue_ifDissolutionForCompanyExists() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(DissolutionFixtures.generateDissolution()));

        final boolean result = service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result);
    }

    @Test
    public void doesDissolutionRequestExistForCompanyByCompanyNumber_returnsFalse_ifDissolutionForCompanyDoesNotExist() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final boolean result = service.doesDissolutionRequestExistForCompanyByCompanyNumber(COMPANY_NUMBER);

        assertFalse(result);
    }

    @Test
    public void doesDissolutionRequestExistForCompanyByApplicationReference_returnsTrue_ifDissolutionForCompanyExists() {
        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.of(DissolutionFixtures.generateDissolution()));

        final boolean result = service.doesDissolutionRequestExistForCompanyByApplicationReference(APPLICATION_REFERENCE);

        assertTrue(result);
    }

    @Test
    public void doesDissolutionRequestExistForCompanyByApplicationReference_returnsFalse_ifDissolutionForCompanyDoesNotExist() {
        when(repository.findByDataApplicationReference(APPLICATION_REFERENCE)).thenReturn(Optional.empty());

        final boolean result = service.doesDissolutionRequestExistForCompanyByApplicationReference(APPLICATION_REFERENCE);

        assertFalse(result);
    }

    @Test
    public void getByCompanyNumber_returnsDissolutionGetResponse() {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(getter.getByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = service.getByCompanyNumber(COMPANY_NUMBER);

        verify(getter).getByCompanyNumber(COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    public void addDirectorApproval_addsDirectorApproval_returnsPatchResponse() throws DissolutionNotFoundException {
        final DissolutionPatchResponse response = DissolutionFixtures.generateDissolutionPatchResponse();

        when(patcher.addDirectorApproval(COMPANY_NUMBER, USER_ID, IP, OFFICER_ID)).thenReturn(response);

        final DissolutionPatchResponse result = service.addDirectorApproval(COMPANY_NUMBER, USER_ID, IP, OFFICER_ID);

        verify(patcher).addDirectorApproval(COMPANY_NUMBER, USER_ID, IP, OFFICER_ID);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    public void hasDirectorAlreadyApproved_callsDissolutionGetter_isDirectorPendingApproval() {
        final String companyNumber = "12345678";
        final String email = "user@mail.com";

        when(getter.isDirectorPendingApproval(companyNumber, email)).thenReturn(true);

        final boolean result = service.isDirectorPendingApproval(companyNumber, email);

        verify(getter).isDirectorPendingApproval(companyNumber, email);

        assertTrue(result);
    }

    @Test
    public void updatePaymentAndSubmissionStatus_updatesPaymentAndSubmissionStatus_returnNothing() throws DissolutionNotFoundException {
        PaymentPatchRequest data = generatePaymentPatchRequest();

        service.handlePayment(data, COMPANY_NUMBER);

        verify(patcher).handlePayment(data.getPaymentReference(), data.getPaidAt(), COMPANY_NUMBER);
    }
}
