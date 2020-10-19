package uk.gov.companieshouse.service.dissolution.chips;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.FeatureToggleConfig;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionVerdictMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.model.enums.VerdictResult;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;
import uk.gov.companieshouse.service.dissolution.DissolutionRefundService;

@Service
public class ChipsResponseService {

    private final DissolutionRepository repository;
    private final DissolutionVerdictMapper dissolutionVerdictMapper;
    private final DissolutionEmailService dissolutionEmailService;
    private final DissolutionRefundService dissolutionRefundService;
    private final Logger logger;
    private final FeatureToggleConfig featureToggleConfig;

    public ChipsResponseService(
            DissolutionRepository repository,
            DissolutionVerdictMapper dissolutionVerdictMapper,
            DissolutionEmailService dissolutionEmailService,
            DissolutionRefundService dissolutionRefundService,
            Logger logger,
            FeatureToggleConfig featureToggleConfig
    ) {
        this.repository = repository;
        this.dissolutionVerdictMapper = dissolutionVerdictMapper;
        this.dissolutionEmailService = dissolutionEmailService;
        this.dissolutionRefundService = dissolutionRefundService;
        this.logger = logger;
        this.featureToggleConfig = featureToggleConfig;
    }

    public void saveAndNotifyDissolutionApplicationOutcome(ChipsResponseCreateRequest body) throws DissolutionNotFoundException {
        DissolutionVerdict dissolutionVerdict = this.dissolutionVerdictMapper.mapToDissolutionVerdict(body);

        Dissolution dissolution = this.repository.findByDataApplicationReference(body.getSubmissionReference()).orElseThrow(DissolutionNotFoundException::new);

        dissolution.setVerdict(dissolutionVerdict);
        dissolution.setActive(false);

        if (featureToggleConfig.isAutomaticallyRequestRefundEnabled() && dissolutionVerdict.getResult() == VerdictResult.REJECTED) {
            dissolutionRefundService.handleRefund(dissolution);
        }

        logger.info(String.format("Received CHIPS verdict for company %s - %s", dissolution.getCompany().getNumber(), dissolutionVerdict.getResult().toString()));

        this.repository.save(dissolution);

        dissolutionEmailService.sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);
    }
}
