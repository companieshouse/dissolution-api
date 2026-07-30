package uk.gov.companieshouse.fixtures;

import com.google.api.client.http.HttpResponseException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;

public class TransactionFixtures {

    public static ApiErrorResponseException generateApiErrorResponseException(int code, String message) {
        return new ApiErrorResponseException(new HttpResponseException.Builder(code, message, new com.google.api.client.http.HttpHeaders()));
    }
}
