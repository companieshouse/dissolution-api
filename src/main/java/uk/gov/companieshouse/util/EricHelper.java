package uk.gov.companieshouse.util;

public class EricHelper {

    public static String getEmail(String authorisedUserHeader) {
        return authorisedUserHeader.split(";")[0];
    }
}
