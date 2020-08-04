package uk.gov.companieshouse.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.companieshouse.config.BarcodeGeneratorConfig;
import uk.gov.companieshouse.model.dto.barcode.BarcodeRequest;
import uk.gov.companieshouse.model.dto.barcode.BarcodeResponse;

@Service
public class BarcodeGeneratorClient {

    private static final String GENERATE_BARCODE_URI = "/";

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String HEADER_CONTENT_TYPE_VALUE = "application/json";

    private final BarcodeGeneratorConfig config;

    @Autowired
    public BarcodeGeneratorClient(BarcodeGeneratorConfig config) {
        this.config = config;
    }

    public BarcodeResponse generateBarcode(BarcodeRequest request) {
        return WebClient
                .create(config.getBarcodeGeneatorHost())
                .post()
                .uri(GENERATE_BARCODE_URI)
                .header(HEADER_AUTHORIZATION, config.getApiKey())
                .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                .body(Mono.just(request), BarcodeRequest.class)
                .retrieve()
                .bodyToMono(BarcodeResponse.class)
                .block();
    }

}
