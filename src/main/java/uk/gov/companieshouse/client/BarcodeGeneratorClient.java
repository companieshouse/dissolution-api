package uk.gov.companieshouse.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.companieshouse.config.BarcodeGeneratorConfig;
import uk.gov.companieshouse.model.dto.barcode.BarcodeRequest;
import uk.gov.companieshouse.model.dto.barcode.BarcodeResponse;

import static uk.gov.companieshouse.model.Constants.HEADER_AUTHORIZATION;
import static uk.gov.companieshouse.model.Constants.HEADER_CONTENT_TYPE;
import static uk.gov.companieshouse.model.Constants.CONTENT_TYPE_JSON;

@Service
public class BarcodeGeneratorClient {


    private final BarcodeGeneratorConfig config;

    @Autowired
    public BarcodeGeneratorClient(BarcodeGeneratorConfig config) {
        this.config = config;
    }

    public BarcodeResponse generateBarcode(BarcodeRequest request) {
        return WebClient
                .create(config.getBarcodeGeneatorHost())
                .post()
                .header(HEADER_AUTHORIZATION, config.getApiKey())
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                .body(Mono.just(request), BarcodeRequest.class)
                .retrieve()
                .bodyToMono(BarcodeResponse.class)
                .block();
    }

}
