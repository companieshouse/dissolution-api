package uk.gov.companieshouse.service.dissolution.chips;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.client.ChipsClient;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import java.util.Collections;
import java.util.List;

@Service
public class DissolutionChipsService {

    private final ChipsClient client;
    private final ChipsSubmitter submitter;

    public DissolutionChipsService(ChipsClient client, ChipsSubmitter submitter) {
        this.client = client;
        this.submitter = submitter;
    }

    public boolean isAvailable() {
        return client.isAvailable();
    }

    public void submitDissolutionsToChips() {
        List<Dissolution> dissolutionsToSubmit = Collections.emptyList(); // TODO
        dissolutionsToSubmit.forEach(submitter::submitDissolutionToChips);
    }
}
