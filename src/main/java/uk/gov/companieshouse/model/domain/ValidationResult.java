package uk.gov.companieshouse.model.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ValidationResult {

    private final List<String> errors = new ArrayList<>();

    public void addError(String error) {
        errors.add(error);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
