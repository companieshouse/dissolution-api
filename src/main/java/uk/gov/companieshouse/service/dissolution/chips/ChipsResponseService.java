package uk.gov.companieshouse.service.dissolution.chips;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionVerdictMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.model.enums.PaymentMethod;
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

    public ChipsResponseService(
            DissolutionRepository repository,
            DissolutionVerdictMapper dissolutionVerdictMapper,
            DissolutionEmailService dissolutionEmailService,
            DissolutionRefundService dissolutionRefundService,
            Logger logger
    ) {
        this.repository = repository;
        this.dissolutionVerdictMapper = dissolutionVerdictMapper;
        this.dissolutionEmailService = dissolutionEmailService;
        this.dissolutionRefundService = dissolutionRefundService;
        this.logger = logger;
    }

    public void saveAndNotifyDissolutionApplicationOutcome(ChipsResponseCreateRequest body) throws DissolutionNotFoundException {
        DissolutionVerdict dissolutionVerdict = this.dissolutionVerdictMapper.mapToDissolutionVerdict(body);

        Dissolution dissolution = this.repository.findByDataApplicationReference(body.getSubmissionReference()).orElseThrow(DissolutionNotFoundException::new);

        dissolution.setVerdict(dissolutionVerdict);
        dissolution.setActive(false);

        if (canRefundDissolution(dissolutionVerdict, dissolution)) {
                dissolutionRefundService.handleRefund(dissolution, dissolutionVerdict);
        }

        logger.info(String.format("Received CHIPS verdict for company %s - %s", dissolution.getCompany().getNumber(), dissolutionVerdict.getResult().toString()));

        this.repository.save(dissolution);

        dissolutionEmailService.sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);
    }

    private boolean canRefundDissolution(DissolutionVerdict dissolutionVerdict, Dissolution dissolution) {
        return dissolutionVerdict.getResult() == VerdictResult.REJECTED &&
            dissolution.getPaymentInformation().getMethod() == PaymentMethod.CREDIT_CARD;
    }
}
