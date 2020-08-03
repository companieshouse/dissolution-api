package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BarcodeGeneratorConfig extends ApiConfig {

    @Value("${barcodeGenerator.host}")
    private String barcodeGeneratorHost;

    public String getBarcodeGeneatorHost() {
        return barcodeGeneratorHost;
    }

    public void setBarcodeGeneatorHost(String barcodeGeneatorHost) {
        this.barcodeGeneratorHost = barcodeGeneatorHost;
    }
}
