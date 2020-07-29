package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentRenderConfig extends ApiConfig {

    @Value("${documentRender.host}")
    private String documentRenderHost;

    public String getDocumentRenderHost() {
        return documentRenderHost;
    }

    public void setDocumentRenderHost(String documentRenderHost) {
        this.documentRenderHost = documentRenderHost;
    }
}
