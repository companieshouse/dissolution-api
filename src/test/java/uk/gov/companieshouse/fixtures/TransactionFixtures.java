package uk.gov.companieshouse.fixtures;

import com.google.api.client.http.HttpResponseException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.companieshouse.model.Constants.LINK_RESOURCE;

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

    public static Transaction buildTransaction(Map<String, Resource> resources) {
        var transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setResources(resources);
        return transaction;
    }

    public static Resource buildResource(String kind, String resourceLink) {
        var resource = new Resource();
        resource.setKind(kind);
        resource.setLinks(Map.of(LINK_RESOURCE, resourceLink));
        return resource;
    }
}
