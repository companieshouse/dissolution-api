package uk.gov.companieshouse.model.dto.chips.xml;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.companieshouse.model.enums.ApplicationType;

import java.util.Arrays;

public enum ChipsFormType {

    DS01("DS01"),
    LLDS01("LLDS01");

    private final String value;

    ChipsFormType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ChipsFormType findByApplicationType(ApplicationType type) {
        return Arrays
                .stream(values())
                .filter(formType -> formType.getValue().equals(type.getValue()))
                .findAny()
                .orElseThrow(() -> new RuntimeException(String.format("Invalid ApplicationType provided: %s", type.getValue())));
    }

}
