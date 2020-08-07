package uk.gov.companieshouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChipsConfig {

    @Value("chips.host")
    private String chipsHost;

    public String getChipsHost() {
        return chipsHost;
    }

    public void setChipsHost(String chipsHost) {
        this.chipsHost = chipsHost;
    }
}
