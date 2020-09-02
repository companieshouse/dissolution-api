package uk.gov.companieshouse.service.dissolution.chips;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DissolutionVerdictMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;

@Service
public class ChipsResponseService {

    private final DissolutionRepository repository;

    private final DissolutionVerdictMapper dissolutionVerdictMapper;

    private final DissolutionEmailService dissolutionEmailService;

    public ChipsResponseService(
            DissolutionRepository repository,
            DissolutionVerdictMapper dissolutionVerdictMapper,
            DissolutionEmailService dissolutionEmailService
    ) {
        this.repository = repository;
        this.dissolutionVerdictMapper = dissolutionVerdictMapper;
        this.dissolutionEmailService = dissolutionEmailService;
    }

    public void saveAndNotifyDissolutionApplicationOutcome(ChipsResponseCreateRequest body) {
        DissolutionVerdict dissolutionVerdict = this.dissolutionVerdictMapper.mapToDissolutionVerdict(body);

        Dissolution dissolution = this.repository.findByDataApplicationReference(body.getSubmissionReference()).get();
        dissolution.setVerdict(dissolutionVerdict);
        this.repository.save(dissolution);

        dissolutionEmailService.sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);
    }
}
