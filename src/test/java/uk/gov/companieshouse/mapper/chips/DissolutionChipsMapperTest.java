package uk.gov.companieshouse.mapper.chips;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;

@ExtendWith(MockitoExtension.class)
public class DissolutionChipsMapperTest {

    private static final String DISSOLUTION_REFERENCE = "someRef";
    private static final String DISSOLUTION_BARCODE = "B4RC0D3";

    private static final byte[] CERTIFICATE_CONTENTS = "some certificate contents".getBytes();

    @InjectMocks
    private DissolutionChipsMapper mapper;

    @Mock
    private ChipsFormDataMapper formDataMapper;

    private Dissolution dissolution;

    @BeforeEach
    public void setup() {
        dissolution = generateDissolution();

        when(formDataMapper.mapToChipsFormDataXml(dissolution)).thenReturn("some xml");
    }

    @Test
    public void mapToDissolutionChipsRequest_setsThePackageMetadataCorrectly() {
        dissolution.getData().getApplication().setReference(DISSOLUTION_REFERENCE);

        final DissolutionChipsRequest request = mapper.mapToDissolutionChipsRequest(dissolution, CERTIFICATE_CONTENTS);

        assertEquals(DISSOLUTION_REFERENCE, request.getPackageMetadata().getId());
        assertEquals(1, request.getPackageMetadata().getCount());
    }

    @Test
    public void mapToDissolutionChipsRequest_generatesTheFormCorrectly() {
        dissolution.getData().getApplication().setBarcode(DISSOLUTION_BARCODE);

        final DissolutionChipsRequest request = mapper.mapToDissolutionChipsRequest(dissolution, CERTIFICATE_CONTENTS);

        verify(formDataMapper).mapToChipsFormDataXml(dissolution);

        assertEquals(1, request.getForms().size());
        assertEquals(DISSOLUTION_BARCODE, request.getForms().get(0).getBarcode());
        assertEquals("some xml", decode(request.getForms().get(0).getXml()));
    }

    @Test
    public void mapToDissolutionChipsRequest_generatesTheFormAttachmentsCorrectly() {
        final DissolutionChipsRequest request = mapper.mapToDissolutionChipsRequest(dissolution, CERTIFICATE_CONTENTS);

        assertEquals(1, request.getForms().get(0).getAttachments().size());
        assertEquals("application/pdf", request.getForms().get(0).getAttachments().get(0).getMimeType());
        assertEquals("FORM IMAGE PDF", request.getForms().get(0).getAttachments().get(0).getCategory());
        assertEquals(new String(CERTIFICATE_CONTENTS), decode(request.getForms().get(0).getAttachments().get(0).getData()));
    }

    private String decode(String encoded) {
        return new String(Base64.getDecoder().decode(encoded));
    }
}
