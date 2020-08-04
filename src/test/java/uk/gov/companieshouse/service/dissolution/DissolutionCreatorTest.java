package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
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

        when(referenceGenerator.generateApplicationReference()).thenReturn(reference);
        when(barcodeGenerator.generateBarcode()).thenReturn(barcode);
        when(requestMapper.mapToDissolution(body, companyNumber, userId, email, ip, reference, barcode)).thenReturn(dissolution);
        when(responseMapper.mapToDissolutionCreateResponse(dissolution)).thenReturn(response);

        final DissolutionCreateResponse result = creator.create(body, companyNumber, userId, ip, email);

        verify(referenceGenerator).generateApplicationReference();
        verify(barcodeGenerator).generateBarcode();
        verify(requestMapper).mapToDissolution(body, companyNumber, userId, email, ip, reference, barcode);
        verify(repository).insert(dissolution);
        verify(responseMapper).mapToDissolutionCreateResponse(dissolution);

        assertEquals(response, result);
    }
}