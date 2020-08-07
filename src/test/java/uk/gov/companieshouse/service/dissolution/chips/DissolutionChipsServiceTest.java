package uk.gov.companieshouse.service.dissolution.chips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.client.ChipsClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DissolutionChipsServiceTest {

    @InjectMocks
    private DissolutionChipsService service;

    @Mock
    private ChipsClient client;

    @Mock
    private ChipsSubmitter submitter;

    @Test
    public void isAvailable_returnsFalse_ifChipsIsNotAvailable() {
        when(client.isAvailable()).thenReturn(false);

        final boolean result = service.isAvailable();

        assertFalse(result);
    }

    @Test
    public void isAvailable_returnsTrue_ifChipsIsAvailable() {
        when(client.isAvailable()).thenReturn(true);

        final boolean result = service.isAvailable();

        assertTrue(result);
    }
}
