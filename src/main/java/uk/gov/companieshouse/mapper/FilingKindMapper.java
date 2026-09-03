package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.model.enums.ApplicationType;

import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

@Component
public class FilingKindMapper {
    public String mapApplicationTypeToFilingKind(ApplicationType applicationType) {
        return switch (applicationType) {
            case DS01 -> FILING_KIND_DS01;
            case LLDS01 -> FILING_KIND_LLDS01;
        };
    }
}
