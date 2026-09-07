package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.model.domain.ValidationResult;

@Component
public class ValidationStatusResponseMapper {

    public ValidationStatusResponse mapToValidationStatusResponse(ValidationResult result) {
        final var errors = result.getErrors().stream()
                .map(this::mapToValidationStatusError)
                .toArray(ValidationStatusError[]::new);

        return new ValidationStatusResponse(errors, result.isValid());
    }

    private ValidationStatusError mapToValidationStatusError(String message) {
        return new ValidationStatusError(message, null, null, null);
    }
}
