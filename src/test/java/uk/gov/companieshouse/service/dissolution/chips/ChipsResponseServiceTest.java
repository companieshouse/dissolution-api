package uk.gov.companieshouse.service.dissolution.chips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.fixtures.ChipsFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionVerdictMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChipsResponseServiceTest {

    @InjectMocks
    private ChipsResponseService chipsResponseService;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionVerdictMapper dissolutionVerdictMapper;

    @Mock
    private DissolutionEmailService dissolutionEmailService;

    @Mock
    private Logger logger;

    @Test
    public void saveAndNotifyDissolutionApplicationOutcome_saveDissolutionApplicationOutcomeAndSendEmail() throws DissolutionNotFoundException {
        ChipsResponseCreateRequest chipsResponseCreateRequest = ChipsFixtures.generateChipsResponseCreateRequest();
        DissolutionVerdict dissolutionVerdict = DissolutionFixtures.generateDissolutionVerdict();
        Dissolution dissolution = DissolutionFixtures.generateDissolution();

        when(dissolutionVerdictMapper.mapToDissolutionVerdict(chipsResponseCreateRequest)).thenReturn(dissolutionVerdict);
        when(repository.findByDataApplicationReference(chipsResponseCreateRequest.getSubmissionReference())).thenReturn(Optional.of(dissolution));

        chipsResponseService.saveAndNotifyDissolutionApplicationOutcome(chipsResponseCreateRequest);
        
        assertFalse(dissolution.getActive());

        verify(dissolutionEmailService).sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);
    }
}
