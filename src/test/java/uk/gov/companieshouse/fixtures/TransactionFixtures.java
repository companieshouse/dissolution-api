package uk.gov.companieshouse.fixtures;

import com.google.api.client.http.HttpResponseException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.model.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.model.Constants.SUBMISSION_URI_PATTERN;

public class TransactionFixtures {

    public static final String TRANSACTION_ID = "tx-id-123";

    public static ApiErrorResponseException generateApiErrorResponseException(int code, String message) {
        return new ApiErrorResponseException(new HttpResponseException.Builder(code, message, new com.google.api.client.http.HttpHeaders()));
    }

    public static Map<String, Object> generateFilingData(Dissolution dissolution) {
        Map<String, Object> data = new HashMap<>();
        data.put("company_name", dissolution.getCompany().getName());
        data.put("company_number", dissolution.getCompany().getNumber());
        return data;
    }

    public static Transaction generateTransaction() {
        return TransactionTestDataBuilder.aTransaction().build();
    }

    public static Transaction generateClosedTransaction() {
        return TransactionTestDataBuilder.aTransaction().withStatus(TransactionStatus.CLOSED).build();
    }

    public static TransactionResourceTestDataBuilder generateTransactionResource(String kind, String dissolutionId) {
        var link = new TransactionResourceTestDataBuilder.Link(LINK_RESOURCE, String.format(SUBMISSION_URI_PATTERN, TRANSACTION_ID, dissolutionId));
        return TransactionResourceTestDataBuilder.aTransactionResource()
                .withResourceKey(String.format(SUBMISSION_URI_PATTERN, TRANSACTION_ID, dissolutionId))
                .withKind(kind)
                .withLinks(link);
    }
}
