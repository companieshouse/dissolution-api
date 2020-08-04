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
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.barcode.BarcodeGenerator;
import uk.gov.companieshouse.service.dissolution.DissolutionCreator;
import uk.gov.companieshouse.service.dissolution.ReferenceGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DissolutionCreatorTest {

    @InjectMocks
    private DissolutionCreator creator;

    @Mock
    ReferenceGenerator referenceGenerator;

    @Mock
    BarcodeGenerator barcodeGenerator;

    @Mock
    DissolutionRequestMapper requestMapper;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionResponseMapper responseMapper;

    public static final String COMPANY_NUMBER = "12345678";
    public static final String COMPANY_NAME = "ComComp";
    public static final String USER_ID = "123";
    public static final String IP = "192.168.0.1";
    public static final String EMAIL = "user@mail.com";
    public static final String REFERENCE = "ABC123";

    @Test
    public void create_generatesAReferenceNumber_mapsToDissolution_savesInDatabase_returnsCreateResponse() {
        final DissolutionCreateRequest body = DissolutionFixtures.generateDissolutionCreateRequest();
        final String companyNumber = "12345678";
        final String userId = "123";
        final String ip = "192.168.0.1";
        final String email = "user@mail.com";

        final String reference = "ABC123";
        final String barcode = "BARC0D3";

        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final DissolutionCreateResponse response = DissolutionFixtures.generateDissolutionCreateResponse();
        final CompanyProfile company = CompanyProfileFixtures.generateCompanyProfile();
        company.setCompanyNumber(COMPANY_NUMBER);
        company.setCompanyName(COMPANY_NAME);

        when(referenceGenerator.generateApplicationReference()).thenReturn(REFERENCE);
        when(barcodeGenerator.generateBarcode()).thenReturn(barcode);
        when(requestMapper.mapToDissolution(body, company, USER_ID, EMAIL, IP, REFERENCE, barcode)).thenReturn(dissolution);
        when(responseMapper.mapToDissolutionCreateResponse(dissolution)).thenReturn(response);

        final DissolutionCreateResponse result = creator.create(body, company, USER_ID, IP, EMAIL);

        verify(referenceGenerator).generateApplicationReference();
        verify(barcodeGenerator).generateBarcode();
        verify(requestMapper).mapToDissolution(body, company, USER_ID, EMAIL, IP, REFERENCE, barcode);
        verify(responseMapper).mapToDissolutionCreateResponse(dissolution);
        verify(repository).insert(dissolution);

        assertEquals(response, result);
    }
}