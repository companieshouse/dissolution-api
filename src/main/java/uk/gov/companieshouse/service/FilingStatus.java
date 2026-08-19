package uk.gov.companieshouse.service;

/**
 * Represents the possible values of {@link uk.gov.companieshouse.api.model.transaction.Filing#getStatus()}.
 * <p>
 * The SDK models this as a plain {@code String}, so this enum exists purely on our side to avoid
 * scattering magic string literals around the codebase.
 */
public enum FilingStatus {

    PROCESSING("processing"),
    ACCEPTED("accepted"),
    REJECTED("rejected");

    private final String value;

    FilingStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean matches(String status) {
        return value.equals(status);
    }
}
