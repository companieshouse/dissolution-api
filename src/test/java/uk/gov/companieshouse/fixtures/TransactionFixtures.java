package uk.gov.companieshouse.fixtures;

import com.google.api.client.http.HttpResponseException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import java.util.HashMap;
import java.util.Map;

public class TransactionFixtures {

    public static ApiErrorResponseException generateApiErrorResponseException(int code, String message) {
        return new ApiErrorResponseException(new HttpResponseException.Builder(code, message, new com.google.api.client.http.HttpHeaders()));
    }

    public static Map<String, Object> generateFilingData(Dissolution dissolution) {
        Map<String, Object> data = new HashMap<>();
        data.put("company_name", dissolution.getCompany().getName());
        data.put("company_number", dissolution.getCompany().getNumber());
        return data;
    }
}
