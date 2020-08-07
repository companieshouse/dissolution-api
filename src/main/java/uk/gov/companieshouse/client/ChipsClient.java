package uk.gov.companieshouse.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.companieshouse.config.ChipsConfig;
import uk.gov.companieshouse.model.dto.chips.DissolutionChipsRequest;

@Service
public class ChipsClient {

    private static class ChipsNotAvailableException extends RuntimeException {}

    private static final String HEALTHCHECK_URI = "/healthcheck/status";
    private static final String POST_FORM_URI = "/efilingEnablement/postForm";

    private final ChipsConfig config;

    @Autowired
    public ChipsClient(ChipsConfig config) {
        this.config = config;
    }

    public boolean isAvailable() {
        try {
            WebClient
                    .create(config.getChipsHost())
                    .get()
                    .uri(HEALTHCHECK_URI)
                    .retrieve()
                    .onStatus(HttpStatus.NOT_FOUND::equals, clientResponse -> { throw new ChipsNotAvailableException(); })
                    .toBodilessEntity()
                    .block();

            return true;
        } catch (ChipsNotAvailableException ex) {
            return false;
        }
    }

    public void sendDissolutionToChips(DissolutionChipsRequest dissolutionRequest) {
        WebClient
                .create(config.getChipsHost())
                .post()
                .uri(POST_FORM_URI)
                .body(Mono.just(dissolutionRequest), DissolutionChipsRequest.class)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
