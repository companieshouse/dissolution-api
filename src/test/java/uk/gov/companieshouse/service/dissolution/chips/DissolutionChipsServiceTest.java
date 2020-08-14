package uk.gov.companieshouse.service.dissolution.chips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import uk.gov.companieshouse.client.ChipsClient;
import uk.gov.companieshouse.config.ChipsConfig;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;

@ExtendWith(MockitoExtension.class)
public class DissolutionChipsServiceTest {

    @InjectMocks
    private DissolutionChipsService service;

    @Mock
    private ChipsClient client;

    @Mock
    private ChipsSubmitter submitter;

    @Mock
    private ChipsConfig config;

    @Mock
    private DissolutionRepository repository;

    private ArgumentCaptor<Dissolution> dissolutionCaptor;

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

    @Test
    public void submitDissolutionsToChips_getsDissolutionsAndSubmitsToChips() {
        dissolutionCaptor = ArgumentCaptor.forClass(Dissolution.class);

        PageRequest limit = PageRequest.of(0, 2, Sort.Direction.ASC, "payment.date_time");
        Dissolution dissolution1 = generateDissolution();
        dissolution1.getCompany().setNumber("1");

        Dissolution dissolution2 = generateDissolution();
        dissolution2.getCompany().setNumber("2");

        List<Dissolution> dissolutions = Arrays.asList(dissolution1, dissolution2);

        when(repository.findPendingDissolutions(any(), eq(limit))).thenReturn(dissolutions);
        when(config.getChipsSubmissionLimit()).thenReturn(2);
        when(config.getChipsRetryDelayMinutes()).thenReturn(30);

        service.submitDissolutionsToChips();

        verify(submitter, times(2)).submitDissolutionToChips(dissolutionCaptor.capture());

        assertEquals(dissolutions.get(0).getId(), dissolutionCaptor.getAllValues().get(0).getId());
        assertEquals(dissolutions.get(1).getId(), dissolutionCaptor.getAllValues().get(1).getId());
    }
}
