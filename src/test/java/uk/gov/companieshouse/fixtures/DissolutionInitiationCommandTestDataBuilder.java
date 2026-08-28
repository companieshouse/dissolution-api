package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.model.dto.dissolution.DirectorRequest;
import uk.gov.companieshouse.model.domain.DissolutionInitiationCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static uk.gov.companieshouse.fixtures.DirectorRequestTestDataBuilder.aDirectorRequest;
import static uk.gov.companieshouse.fixtures.TransactionTestDataBuilder.aTransaction;

public class DissolutionInitiationCommandTestDataBuilder {

    private Transaction transaction = aTransaction().build();
    private String companyNumber = "12345678";
    private String userId = "123";
    private List<DirectorRequest> signatories = Collections.singletonList(aDirectorRequest().build());

    public static DissolutionInitiationCommandTestDataBuilder aDissolutionInitiationCommand() {
        return new DissolutionInitiationCommandTestDataBuilder();
    }

    public DissolutionInitiationCommandTestDataBuilder withTransaction(Transaction transaction) {
        this.transaction = transaction;
        return this;
    }

    public DissolutionInitiationCommandTestDataBuilder withTransaction(TransactionTestDataBuilder transactionTestDataBuilder) {
        this.transaction = transactionTestDataBuilder.build();
        return this;
    }

    public DissolutionInitiationCommandTestDataBuilder withCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public DissolutionInitiationCommandTestDataBuilder withUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public DissolutionInitiationCommandTestDataBuilder withSignatories(List<DirectorRequest> signatories) {
        this.signatories = signatories;
        return this;
    }

    public DissolutionInitiationCommandTestDataBuilder withSignatories(DirectorRequestTestDataBuilder... signatoryBuilders) {
        return withSignatories(Arrays.stream(signatoryBuilders).map(DirectorRequestTestDataBuilder::build).toList());
    }

    public DissolutionInitiationCommand build() {
        return new DissolutionInitiationCommand(transaction, companyNumber, userId, signatories);
    }
}
