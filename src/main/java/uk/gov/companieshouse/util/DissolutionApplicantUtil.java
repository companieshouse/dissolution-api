package uk.gov.companieshouse.util;

import uk.gov.companieshouse.model.db.dissolution.Dissolution;

public class DissolutionApplicantUtil {

    private DissolutionApplicantUtil() {}

    public static boolean doesEmailBelongToApplicant(String email, Dissolution dissolution) {
        return dissolution
                .getCreatedBy()
                .getEmail()
                .equals(email);
    }

}
