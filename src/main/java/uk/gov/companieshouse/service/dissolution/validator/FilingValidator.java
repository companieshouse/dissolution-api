package uk.gov.companieshouse.service.dissolution.validator;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.ValidationResult;
import uk.gov.companieshouse.model.enums.DissolutionStatus;


@Service
public class FilingValidator {

    public ValidationResult validate(Dissolution dissolution) {
        final var result = new ValidationResult();

        checkStatusIsSubmitted(dissolution, result);
        checkAllSignatoriesHaveApproved(dissolution, result);

        return result;
    }

    private void checkStatusIsSubmitted(Dissolution dissolution, ValidationResult result) {
        if (dissolution.getStatus() != DissolutionStatus.SUBMITTED) {
            result.addError(String.format(
                    "Dissolution status is %s, expected %s", dissolution.getStatus(), DissolutionStatus.SUBMITTED));
        }
    }

    private void checkAllSignatoriesHaveApproved(Dissolution dissolution, ValidationResult result) {
        final var allSignatoriesHaveApproved = dissolution.getSignatories()
                .stream()
                .allMatch(signatory -> signatory.getDirectorApproval() != null);

        if (!allSignatoriesHaveApproved) {
            result.addError("Not all signatories have signed");
        }
    }
}
