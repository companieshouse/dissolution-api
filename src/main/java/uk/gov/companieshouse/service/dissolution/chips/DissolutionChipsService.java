package uk.gov.companieshouse.service.dissolution.chips;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.ChipsClient;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.companieshouse.model.Constants.CHIPS_RETRY_DELAY_MINUTES;
import static uk.gov.companieshouse.model.Constants.CHIPS_SUBMISSION_LIMIT;

@Service
public class DissolutionChipsService {

    private final DissolutionRepository repository;

    private final ChipsClient client;
    private final ChipsSubmitter submitter;

    public DissolutionChipsService(ChipsClient client, ChipsSubmitter submitter, DissolutionRepository repository) {
        this.client = client;
        this.submitter = submitter;
        this.repository = repository;
    }

    public boolean isAvailable() {
        return client.isAvailable();
    }

    public void submitDissolutionsToChips() {
        List<Dissolution> dissolutionsToSubmit = getPendingDissolutions();
        dissolutionsToSubmit.forEach(submitter::submitDissolutionToChips);
    }

    private List<Dissolution> getPendingDissolutions() {
        return repository.findPendingDissolutions(
                LocalDateTime.now().minusMinutes(CHIPS_RETRY_DELAY_MINUTES),
                PageRequest.of(0, CHIPS_SUBMISSION_LIMIT, Sort.Direction.ASC, "payment.date_time"));
    }
}
