package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DissolutionConfig {

    @Value("${dissolution.pdfBucket}")
    private String dissolutionPdfBucket;

    public String getDissolutionPdfBucket() {
        return dissolutionPdfBucket;
    }

    public void setDissolutionPdfBucket(String dissolutionPdfBucket) {
        this.dissolutionPdfBucket = dissolutionPdfBucket;
    }
}
