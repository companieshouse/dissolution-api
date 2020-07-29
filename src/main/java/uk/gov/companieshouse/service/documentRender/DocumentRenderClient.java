package uk.gov.companieshouse.service.documentRender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.companieshouse.config.DocumentRenderConfig;
import uk.gov.companieshouse.model.dto.documentRender.DissolutionCertificateData;

@Service
public class DocumentRenderClient {

    private static final String DOCUMENT_RENDER_STORE_ENDPOINT = "/document-render/store";

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_ASSET_ID = "assetID";
    private static final String HEADER_TEMPLATE_NAME = "templateName";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_LOCATION = "Location";

    private static final String HEADER_ASSET_ID_VALUE = "dissolution";
    private static final String HEADER_ACCEPT_VALUE = "application/pdf";
    private static final String HEADER_CONTENT_TYPE_VALUE = "text/html";

    private final DocumentRenderConfig config;

    public DocumentRenderClient(DocumentRenderConfig config) {
        this.config = config;
    }

    public String generateAndStoreDocument(DissolutionCertificateData data, String templateName, String location) {
        return WebClient
                .create(config.getDocumentRenderHost())
                .post()
                .uri(DOCUMENT_RENDER_STORE_ENDPOINT)
                .header(HEADER_AUTHORIZATION, config.getApiKey())
                .header(HEADER_ASSET_ID, HEADER_ASSET_ID_VALUE)
                .header(HEADER_TEMPLATE_NAME, templateName)
                .header(HEADER_ACCEPT, HEADER_ACCEPT_VALUE)
                .header(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_VALUE)
                .header(HEADER_LOCATION, location)
                .body(Mono.just(asJsonString(data)), String.class)
                .exchange()
                .block()
                .headers()
                .header(HEADER_LOCATION)
                .stream()
                .findAny()
                .orElseThrow(() -> new RuntimeException("No location header returned from Document Render Service"));
    }

    private String asJsonString(DissolutionCertificateData data) {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to write dissolution certificate data to JSON string");
        }
    }
}
