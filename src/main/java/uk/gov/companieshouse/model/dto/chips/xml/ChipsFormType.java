package uk.gov.companieshouse.model.dto.chips.xml;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.companieshouse.model.enums.ApplicationType;

import java.util.Map;

public enum ChipsFormType {

    DS01("DS01"),
    LLDS01("LLDS01");

    private static final Map<ApplicationType, ChipsFormType> FORM_TYPE_MAPPING = Map.of(
            ApplicationType.DS01, ChipsFormType.DS01,
            ApplicationType.LLDS01, ChipsFormType.LLDS01
    );

    private final String value;

    ChipsFormType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ChipsFormType findByApplicationType(ApplicationType type) {
        return FORM_TYPE_MAPPING.get(type);
    }

}
