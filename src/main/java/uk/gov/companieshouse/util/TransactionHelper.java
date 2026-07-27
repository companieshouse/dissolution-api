package uk.gov.companieshouse.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import java.util.Objects;
import java.util.UUID;

import static uk.gov.companieshouse.model.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.model.Constants.SUBMISSION_URI_PATTERN;

@Component
public class TransactionHelper {

    public boolean isTransactionLinkedToDissolution(Transaction transaction, String submissionSelfLink) {
        if (StringUtils.isBlank(submissionSelfLink)) {
            return false;
        }

        if (Objects.isNull(transaction) || Objects.isNull(transaction.getResources())) {
            return false;
        }

        return transaction.getResources().entrySet().stream()
                .filter(resource -> FILING_KIND_OVERSEAS_ENTITY.equals(resource.getValue().getKind()))
                .anyMatch(resource -> submissionSelfLink.equals(resource.getValue().getLinks().get(LINK_RESOURCE)));
    }

    public String getSubmissionUri(String transactionId, String submissionId) {
        return String.format(SUBMISSION_URI_PATTERN, transactionId, submissionId);
    }
}