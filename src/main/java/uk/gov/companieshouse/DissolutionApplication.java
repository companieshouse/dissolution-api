package uk.gov.companieshouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
public class DissolutionApplication {

    public static void main (String[] args) {
        SpringApplication.run(DissolutionApplication.class, args);
    }

    @Bean
    @Primary
    public ObjectMapper configureJsonMapper() {
        return new ObjectMapper();
    }

    @Bean(name = "xmlMapper")
    public XmlMapper configureXmlMapper() {
        final XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        return xmlMapper;
    }
}
