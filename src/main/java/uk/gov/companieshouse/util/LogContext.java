package uk.gov.companieshouse.util;

import uk.gov.companieshouse.model.LogAction;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.model.Constants.ACTION_KEY;
import static uk.gov.companieshouse.model.Constants.COMPANY_NUMBER_KEY;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.model.Constants.USER_ID_KEY;

public final class LogContext {

    private LogContext() {
    }

    public static Map<String, Object> forAction(final LogAction action, final String companyNumber,
                                                final String transactionId, final String userId) {
        final var context = new HashMap<String, Object>();
        context.put(ACTION_KEY, action.getValue());
        context.put(COMPANY_NUMBER_KEY, companyNumber);
        context.put(TRANSACTION_ID_KEY, transactionId);
        context.put(USER_ID_KEY, userId);
        return context;
    }
}
