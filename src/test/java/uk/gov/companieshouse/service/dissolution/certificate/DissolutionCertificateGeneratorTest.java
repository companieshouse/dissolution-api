package uk.gov.companieshouse.service.dissolution.certificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.DocumentRenderClient;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionCertificateMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionCertificate;
import uk.gov.companieshouse.model.dto.documentrender.DissolutionCertificateData;
import uk.gov.companieshouse.model.enums.ApplicationType;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;

@ExtendWith(MockitoExtension.class)
public class DissolutionCertificateGeneratorTest {

    private static final String LOCATION = "some-location";
    private static final String SAVED_LOCATION = "some-saved-location";

    @InjectMocks
    private DissolutionCertificateGenerator certificateGenerator;

    @Mock
    private DissolutionCertificateMapper mapper;

    @Mock
    private DissolutionCertificateLocationGenerator locationGenerator;

    @Mock
    private DocumentRenderClient client;

    @Mock
    private Logger logger;

    private Dissolution dissolution;

    private DissolutionCertificateData dissolutionCertificateData;

    @BeforeEach
    public void setUp() {
        dissolution = generateDissolution();
        dissolutionCertificateData = generateDissolutionCertificateData();
    }

    @Test
    public void generateDissolutionCertificate_generatesDataAndLocationForCertificate() {
        when(mapper.mapToCertificateData(dissolution)).thenReturn(dissolutionCertificateData);
        when(locationGenerator.generateCertificateLocation(dissolution)).thenReturn(LOCATION);

        certificateGenerator.generateDissolutionCertificate(dissolution);

        verify(mapper).mapToCertificateData(dissolution);
        verify(locationGenerator).generateCertificateLocation(dissolution);
        verify(client).generateAndStoreDocument(eq(dissolutionCertificateData), anyString(), eq(LOCATION));
    }

    @Test
    public void generateDissolutionCertificate_generatesDS01Certificate_whenApplicationIsADS01() {
        dissolution.getData().getApplication().setType(ApplicationType.DS01);

        when(mapper.mapToCertificateData(dissolution)).thenReturn(dissolutionCertificateData);
        when(locationGenerator.generateCertificateLocation(dissolution)).thenReturn(LOCATION);

        certificateGenerator.generateDissolutionCertificate(dissolution);

        verify(client).generateAndStoreDocument(dissolutionCertificateData, "ds01.html", LOCATION);
    }

    @Test
    public void generateDissolutionCertificate_mapsTheReturnedCertificateLocationToADissolutionCertificate() {
        final DissolutionCertificate certificate = generateDissolutionCertificate();

        when(mapper.mapToCertificateData(dissolution)).thenReturn(dissolutionCertificateData);
        when(locationGenerator.generateCertificateLocation(dissolution)).thenReturn(LOCATION);
        when(client.generateAndStoreDocument(dissolutionCertificateData, "ds01.html", LOCATION)).thenReturn(SAVED_LOCATION);
        when(mapper.mapToDissolutionCertificate(SAVED_LOCATION)).thenReturn(certificate);

        final DissolutionCertificate result = certificateGenerator.generateDissolutionCertificate(dissolution);

        assertEquals(certificate, result);

        verify(mapper).mapToDissolutionCertificate(SAVED_LOCATION);
    }
}
