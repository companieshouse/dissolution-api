package uk.gov.companieshouse.mapper.mapper;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.mapper.chips.ChipsFormDataMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;

@ExtendWith(MockitoExtension.class)
public class ChipsFormDataMapperTest {

    @InjectMocks
    private ChipsFormDataMapper mapper;

    @Mock
    private XmlMapper xmlMapper;

    private Dissolution dissolution;

    @BeforeEach
    public void setup() throws Exception {
        dissolution = generateDissolution();

        when(xmlMapper.writeValueAsString(any())).thenReturn("some xml");
    }

    @Test
    public void mapToChipsFormDataXml_writesToAnXmlString_andReturnsIt() throws Exception {
        final String result = mapper.mapToChipsFormDataXml(dissolution);

        assertEquals("some xml", result);
    }
}
