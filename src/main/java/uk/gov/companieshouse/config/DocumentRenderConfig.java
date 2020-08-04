package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentRenderConfig extends ApiConfig {

    @Value("${documentRender.host}")
    private String documentRenderHost;

    @Value("${cdn.host}")
    private String cdnHost;

    public String getDocumentRenderHost() {
        return documentRenderHost;
    }

    public void setDocumentRenderHost(String documentRenderHost) {
        this.documentRenderHost = documentRenderHost;
    }

    public String getCdnHost() {
        return cdnHost;
    }

    public void setCdnHost(String cdnHost) {
        this.cdnHost = cdnHost;
    }
}
