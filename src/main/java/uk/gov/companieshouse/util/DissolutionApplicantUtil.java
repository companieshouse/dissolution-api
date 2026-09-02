package uk.gov.companieshouse.util;

import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import java.util.Objects;

public class DissolutionApplicantUtil {

    private DissolutionApplicantUtil() {}

    public static boolean doesEmailBelongToApplicant(String email, Dissolution dissolution) {
        return dissolution
                .getCreatedBy()
                .getEmail()
                .equals(email);
    }

    public static boolean isApplicant(String userId, Dissolution dissolution) {
        var createdBy = dissolution.getCreatedBy();
        return createdBy != null && Objects.equals(createdBy.getUserId(), userId);
    }
}
