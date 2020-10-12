package uk.gov.companieshouse.service.dissolution.validator;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.FeatureToggleConfig;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CompanyOfficerValidator {

    private static final String ERROR_DUPLICATE_DIRECTOR_ID = "Officer IDs must be unique";
    private static final String ERROR_DUPLICATE_DIRECTOR_EMAIL = "Director emails must be unique";
    private static final String ERROR_DIRECTOR_NOT_FOUND = "One or more officer IDs are not valid";
    private static final String ERROR_MAJORITY_NOT_SELECTED = "The majority of active company directors must be provided as signatories";

    private final FeatureToggleConfig featureToggleConfig;

    public CompanyOfficerValidator(FeatureToggleConfig featureToggleConfig) {
        this.featureToggleConfig = featureToggleConfig;
    }

    public Optional<String> areSelectedDirectorsValid(Map<String, CompanyOfficer> companyDirectors, List<DirectorRequest> selectedDirectors) {
        if (!areDirectorIdsUnique(selectedDirectors)) {
            return Optional.of(ERROR_DUPLICATE_DIRECTOR_ID);
        }

        if (!areDirectorEmailsUnique(selectedDirectors)) {
            return Optional.of(ERROR_DUPLICATE_DIRECTOR_EMAIL);
        }

        if (!doDirectorsExist(companyDirectors, selectedDirectors)) {
            return Optional.of(ERROR_DIRECTOR_NOT_FOUND);
        }

        if (!isMajorityOfDirectorsSelected(companyDirectors, selectedDirectors)) {
            return Optional.of(ERROR_MAJORITY_NOT_SELECTED);
        }

        return Optional.empty();
    }

    private boolean areDirectorIdsUnique(List<DirectorRequest> selectedDirectors) {
        return selectedDirectors
                .stream()
                .map(DirectorRequest::getOfficerId)
                .distinct()
                .count() == selectedDirectors.size();
    }

    private boolean areDirectorEmailsUnique(List<DirectorRequest> selectedDirectors) {
        return !featureToggleConfig.isUniqueEmailsEnabled() || selectedDirectors
                .stream()
                .map(director -> director.getEmail().toLowerCase())
                .distinct()
                .count() == selectedDirectors.size();
    }

    private boolean doDirectorsExist(Map<String, CompanyOfficer> companyDirectors, List<DirectorRequest> selectedDirectors) {
        return selectedDirectors.stream().allMatch(director -> companyDirectors.get(director.getOfficerId()) != null);
    }

    private boolean isMajorityOfDirectorsSelected(Map<String, CompanyOfficer> companyDirectors, List<DirectorRequest> selectedDirectors) {
        return ((float) selectedDirectors.size() / (float) companyDirectors.size()) > 0.5;
    }
}
