package uk.gov.companieshouse.service.dissolution;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

@Service
public class ReferenceGenerator {
    public String generateApplicationReference() {
        return RandomStringUtils.random(6, true, true).toUpperCase();
    }
}
