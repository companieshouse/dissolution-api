package uk.gov.companieshouse.service.dissolution.validator;

import org.apache.commons.lang3.StringUtils;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.InvalidTransactionStateException;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;
import static uk.gov.companieshouse.model.Constants.LINK_RESOURCE;
import static uk.gov.companieshouse.model.Constants.DISSOLUTION_BASE_URI_PATTERN;

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
            if (!transactionStatus.equals(tx.getStatus())) {
                throw new InvalidTransactionStateException(String.format("Transaction status %s does not match expected status %s", tx.getStatus(), transactionStatus));
            }
        });
    }

    public TransactionValidator forCompany(String companyNumber) {
        return addRule(tx -> {
            if (!(StringUtils.isNotBlank(companyNumber) && companyNumber.equals(tx.getCompanyNumber()))) {
                throw new InvalidTransactionStateException("Transaction does not belong to company " + companyNumber);
            }
        });
    }

    public TransactionValidator isLinkedToDissolution(Dissolution dissolution) {
        return addRule(tx -> {
            final String companyNumber = dissolution.getCompany().getNumber();
            final String submissionSelfLink = String.format(DISSOLUTION_BASE_URI_PATTERN, companyNumber, dissolution.getTransactionId());

            final boolean isLinked = Objects.nonNull(tx.getResources()) && tx.getResources().values().stream()
                    .filter(resource -> DISSOLUTION_FILING_KINDS.contains(resource.getKind()))
                    .anyMatch(resource -> submissionSelfLink.equals(Objects.requireNonNullElseGet(resource.getLinks(), Map::of).get(LINK_RESOURCE)));

            if (!isLinked) {
                throw new DissolutionNotLinkedToTransactionException(
                        String.format("Transaction %s is not linked to a dissolution for company %s", tx.getId(), companyNumber));
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
