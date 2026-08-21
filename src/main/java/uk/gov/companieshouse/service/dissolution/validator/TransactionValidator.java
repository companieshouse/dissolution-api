package uk.gov.companieshouse.service.dissolution.validator;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;

import java.util.Objects;
import java.util.Set;

import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

// TODO: Move TransactionHelper into this validator
public class TransactionValidator {
//    private static final Set<String> DISSOLUTION_FILING_KINDS = Set.of(FILING_KIND_DS01, FILING_KIND_LLDS01);

    private final Transaction transaction;

    public TransactionValidator(Transaction transaction) {
        this.transaction = Objects.requireNonNull(transaction);
    }

//    public boolean isLinkedToDissolution(String dissolutionId) {
//        if (!isValid() || Objects.isNull(transaction.getResources()) || StringUtils.isBlank(dissolutionId)) {
//            return false;
//        }
//
//        final String submissionSelfLink = String.format(SUBMISSION_URI_PATTERN, transaction.getId(), dissolutionId);
//
//        return transaction.getResources().values().stream()
//                .filter(resource -> DISSOLUTION_FILING_KINDS.contains(resource.getKind()))
//                .anyMatch(resource -> submissionSelfLink.equals(resource.getLinks().get(LINK_RESOURCE)));
//    }

    public boolean hasStatus(TransactionStatus status) {
        return Objects.nonNull(transaction.getStatus()) && status.equals(transaction.getStatus());
    }

//    public boolean isValid() {
//        return StringUtils.isNotBlank(transaction.getId())
//                && Objects.nonNull(transaction.getStatus())
//                && Objects.nonNull(transaction.getCompanyNumber());
//    }

    public boolean isLinkedToCompany(String companyNumber) {
        return StringUtils.isNotBlank(companyNumber) && companyNumber.equals(transaction.getCompanyNumber());
    }
}
