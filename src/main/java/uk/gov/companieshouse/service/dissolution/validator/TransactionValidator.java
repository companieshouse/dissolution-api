package uk.gov.companieshouse.service.dissolution.validator;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.InvalidTransactionStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;
import static uk.gov.companieshouse.model.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.model.Constants.SUBMISSION_URI_PATTERN;

public class TransactionValidator {
    private static final Set<String> DISSOLUTION_FILING_KINDS = Set.of(FILING_KIND_DS01, FILING_KIND_LLDS01);

    private final Transaction transaction;
    private final List<Consumer<Transaction>> rules = new ArrayList<>();

    private TransactionValidator(Transaction transaction) {
        this.transaction = transaction;
    }

    public static TransactionValidator of(Transaction transaction) {
        return new TransactionValidator(Objects.requireNonNull(transaction, "valid transaction required to validate"));
    }

    public TransactionValidator hasStatus(TransactionStatus transactionStatus) {
        return addRule(tx -> {
            if (!transactionStatus.equals(transaction.getStatus())) {
                throw new InvalidTransactionStateException(String.format("Transaction status %s does not match expected status %s", transaction.getStatus(), transactionStatus));
            }
        });
    }

    public TransactionValidator forCompany(String companyNumber) {
        return addRule(tx -> {
            if (!(StringUtils.isNotBlank(companyNumber) && companyNumber.equals(transaction.getCompanyNumber()))) {
                throw new InvalidTransactionStateException("Transaction does not belong to company " + companyNumber);
            }
        });
    }

    public TransactionValidator isLinkedToDissolution(String dissolutionId) {
        return addRule(tx -> {
            final String submissionSelfLink = String.format(SUBMISSION_URI_PATTERN, transaction.getId(), dissolutionId);

            final boolean isLinked = Objects.nonNull(transaction.getResources()) && transaction.getResources().values().stream()
                    .filter(resource -> DISSOLUTION_FILING_KINDS.contains(resource.getKind()))
                    .anyMatch(resource -> submissionSelfLink.equals(resource.getLinks().get(LINK_RESOURCE)));

            if (!isLinked) {
                throw new DissolutionNotLinkedToTransactionException("Transaction is not linked to dissolution " + dissolutionId);
            }
        });
    }

    public void validate() {
        rules.forEach(rule -> rule.accept(transaction));
    }

    private TransactionValidator addRule(Consumer<Transaction> rule) {
        rules.add(rule);
        return this;
    }
}
