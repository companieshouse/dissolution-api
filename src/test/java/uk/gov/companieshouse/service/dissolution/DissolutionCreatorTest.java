package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.CompanyProfileFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionUserDataMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.DissolutionUserData;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.barcode.BarcodeGenerator;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.CompanyOfficerFixtures.generateCompanyOfficer;

@ExtendWith(MockitoExtension.class)
public class DissolutionCreatorTest {

    @InjectMocks
    private DissolutionCreator creator;

    @Mock
    private ReferenceGenerator referenceGenerator;

    @Mock
    private BarcodeGenerator barcodeGenerator;

    @Mock
    private DissolutionRequestMapper requestMapper;

    @Mock
    private DissolutionUserDataMapper userDataMapper;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionResponseMapper responseMapper;

    @Mock
    private DissolutionEmailService emailService;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String USER_ID = "123";
    public static final String IP = "192.168.0.1";
    public static final String EMAIL = "user@mail.com";
    public static final String REFERENCE = "ABC123";
    public static final String BARCODE = "BARC0D3";

    @Test
    public void create_generatesAReferenceNumber_mapsToDissolution_savesInDatabase_notifiesSignatories_returnsCreateResponse() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();

        final DissolutionUserData userData = DissolutionFixtures.generateDissolutionUserData();
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final DissolutionCreateResponse response = DissolutionFixtures.generateDissolutionCreateResponse();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        final Map<String, CompanyOfficer> directors = Map.of("abc123", generateCompanyOfficer());

        when(referenceGenerator.generateApplicationReference()).thenReturn(REFERENCE);
        when(barcodeGenerator.generateBarcode()).thenReturn(BARCODE);
        when(requestMapper.mapToDissolution(body, company, directors, userData, REFERENCE, BARCODE)).thenReturn(dissolution);
        when(responseMapper.mapToDissolutionCreateResponse(dissolution)).thenReturn(response);
        when(userDataMapper.mapToUserData(USER_ID, IP, EMAIL)).thenReturn(userData);

        final DissolutionCreateResponse result = creator.create(body, company, directors, USER_ID, IP, EMAIL);

        verify(referenceGenerator).generateApplicationReference();
        verify(barcodeGenerator).generateBarcode();
        verify(requestMapper).mapToDissolution(body, company, directors, userData, REFERENCE, BARCODE);
        verify(repository).insert(dissolution);
        verify(emailService).notifySignatoriesToSign(dissolution);
        verify(responseMapper).mapToDissolutionCreateResponse(dissolution);

        assertEquals(response, result);
    }
}