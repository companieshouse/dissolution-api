package uk.gov.companieshouse.util;

public class EricHelper {

    private EricHelper() {}

    public static String getEmail(String authorisedUserHeader) {
        return authorisedUserHeader.split(";")[0];
    }
}
