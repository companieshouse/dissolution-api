package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionCreator;
import uk.gov.companieshouse.service.dissolution.DissolutionGetter;
import uk.gov.companieshouse.service.dissolution.DissolutionPatcher;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    public static final String COMPANY_NAME = "ComComp";
    public static final String USER_ID = "123";
    public static final String IP = "192.168.0.1";
    public static final String EMAIL = "user@mail.com";

    @Test
    public void create_createsADissolutionRequest_returnsCreateResponse() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final DissolutionCreateResponse response = DissolutionFixtures.generateDissolutionCreateResponse();
        final CompanyProfileApi company = CompanyProfileFixtures.generateCompanyProfileApi();

        when(creator.create(body, company, USER_ID, IP, EMAIL)).thenReturn(response);

        final DissolutionCreateResponse result = service.create(body, company, USER_ID, IP, EMAIL);

        verify(creator).create(body, company, USER_ID, IP, EMAIL);

        assertEquals(response, result);
    }

    @Test
    public void doesDissolutionRequestExistForCompany_returnsTrue_ifDissolutionForCompanyExists() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.of(DissolutionFixtures.generateDissolution()));

        final boolean result = service.doesDissolutionRequestExistForCompany(COMPANY_NUMBER);

        assertTrue(result);
    }

    @Test
    public void doesDissolutionRequestExistForCompany_returnsFalse_ifDissolutionForCompanyDoesNotExist() {
        when(repository.findByCompanyNumber(COMPANY_NUMBER)).thenReturn(Optional.empty());

        final boolean result = service.doesDissolutionRequestExistForCompany(COMPANY_NUMBER);

        assertFalse(result);
    }

    @Test
    public void get_getsADissolution_returnsGetResponse() throws Exception {
        final DissolutionGetResponse response = DissolutionFixtures.generateDissolutionGetResponse();

        when(getter.get(COMPANY_NUMBER)).thenReturn(Optional.of(response));

        final Optional<DissolutionGetResponse> result = service.get(COMPANY_NUMBER);

        verify(getter).get(COMPANY_NUMBER);

        assertTrue(result.isPresent());
        assertEquals(response, result.get());
    }

    @Test
    public void addDirectorApproval_addsDirectorApproval_returnsPatchResponse() throws Exception {
        final DissolutionPatchResponse response = DissolutionFixtures.generateDissolutionPatchResponse();

        when(patcher.addDirectorApproval(COMPANY_NUMBER, USER_ID, IP, EMAIL)).thenReturn(response);

        final DissolutionPatchResponse result = service.addDirectorApproval(COMPANY_NUMBER, USER_ID, IP, EMAIL);

        verify(patcher).addDirectorApproval(COMPANY_NUMBER, USER_ID, IP, EMAIL);

        assertNotNull(result);
        assertEquals(response, result);
    }

    @Test
    public void hasDirectorAlreadyApproved_callsDissolutionGetter_isDirectorPendingApproval() throws Exception {
        final String companyNumber = "12345678";
        final String email = "user@mail.com";

        when(getter.isDirectorPendingApproval(companyNumber, email)).thenReturn(true);

        final boolean result = service.isDirectorPendingApproval(companyNumber, email);

        verify(getter).isDirectorPendingApproval(companyNumber, email);

        assertTrue(result);
    }

    @Test
    public void updatePaymentStatus_updatesPaymentStatus_returnNothing() {
        PaymentPatchRequest data = generatePaymentPatchRequest();

        service.updatePaymentStatus(data, COMPANY_NUMBER);

        verify(patcher).updatePaymentInformation(data.getPaymentReference(), data.getPaidAt(), COMPANY_NUMBER);
    }
}
