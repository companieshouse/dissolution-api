package uk.gov.companieshouse;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DissolutionApplication {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DissolutionApplication.class);

    public static void main (String[] args) {
        LOGGER.info("Starting Dissolution API...");
        SpringApplication.run(DissolutionApplication.class, args);
    }
}
