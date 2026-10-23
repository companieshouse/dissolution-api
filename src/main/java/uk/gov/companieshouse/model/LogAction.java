package uk.gov.companieshouse.model;

/**
 * Stable action names written to the "action" log field. Dashboards, metric filters and
 * alarms match on these values, so do not rename one without updating them.
 */
public enum LogAction {

    DRAFT_DISSOLUTION_REQUESTED("draft_dissolution_requested");

    private final String value;

    LogAction(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
