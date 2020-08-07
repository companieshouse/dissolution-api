package uk.gov.companieshouse;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DissolutionApplication {

    public static void main (String[] args) {
        SpringApplication.run(DissolutionApplication.class, args);
    }

    @Bean
    public XmlMapper configureXmlMapper() {
        return new XmlMapper();
    }
}
