package uk.gov.companieshouse.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;

import java.util.Objects;
import java.util.Set;

import static uk.gov.companieshouse.model.Constants.*;

@Component
public class TransactionHelper {

    private static final Set<String> DISSOLUTION_FILING_KINDS = Set.of(FILING_KIND_DS01, FILING_KIND_LLDS01);

    public boolean isTransactionLinkedToDissolution(Transaction transaction, String submissionId) {
        if (Objects.isNull(transaction) || Objects.isNull(transaction.getResources())) {
            return false;
        }

        String submissionSelfLink = String.format(SUBMISSION_URI_PATTERN, transaction.getId(), submissionId);

        if (StringUtils.isBlank(submissionSelfLink)) {
            return false;
        }

        return transaction.getResources().entrySet().stream()
                .filter(resource -> DISSOLUTION_FILING_KINDS.contains(resource.getValue().getKind()))
                .anyMatch(resource -> submissionSelfLink.equals(resource.getValue().getLinks().get(LINK_RESOURCE)));
    }
}
