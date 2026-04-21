package uk.gov.companieshouse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlWriteFeature;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@SpringBootApplication
public class DissolutionApplication {

    @Value("${logging.namespace}")
    private String loggerNamespace;

    public static void main (String[] args) {
        SpringApplication.run(DissolutionApplication.class, args);
    }

    @Bean(name = "xmlMapper")
    public XmlMapper configureXmlMapper() {
        return XmlMapper.xmlBuilder()
                .configure(XmlWriteFeature.WRITE_XML_DECLARATION, true)
                .build();
    }

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(loggerNamespace);
    }
}
