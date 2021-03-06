package uk.gov.companieshouse.service.dissolution.chips;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.ChipsClient;
import uk.gov.companieshouse.config.ChipsConfig;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.List;

import static uk.gov.companieshouse.util.DateTimeGenerator.generateCurrentDateTime;

@Service
public class DissolutionChipsService {

    private final DissolutionRepository repository;

    private static final int PAGE_NUMBER_INDEX = 0;
    private final ChipsClient client;
    private final ChipsSubmitter submitter;
    private final ChipsConfig config;
    
    public DissolutionChipsService(ChipsClient client, ChipsSubmitter submitter,
                                   DissolutionRepository repository, ChipsConfig config) {
        this.client = client;
        this.submitter = submitter;
        this.repository = repository;
        this.config = config;
    }

    public boolean isAvailable() {
        return client.isAvailable();
    }

    public void submitDissolutionsToChips() {
        List<Dissolution> dissolutionsToSubmit = getPendingDissolutions();
        dissolutionsToSubmit.parallelStream().forEach(submitter::submitDissolutionToChips);
    }

    private List<Dissolution> getPendingDissolutions() {
        return repository.findPendingDissolutions(
                generateCurrentDateTime().minusMinutes(config.getChipsRetryDelayMinutes()),
                PageRequest.of(PAGE_NUMBER_INDEX, config.getChipsSubmissionLimit(), Sort.Direction.ASC, "payment.date_time"));
    }
}
