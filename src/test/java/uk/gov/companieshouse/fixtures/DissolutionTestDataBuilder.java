package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.db.dissolution.Company;
import uk.gov.companieshouse.model.db.dissolution.CreatedBy;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionData;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.dissolution.DissolutionSubmission;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DissolutionTestDataBuilder {

    private LocalDateTime modifiedDateTime = LocalDateTime.now();
    private DissolutionData data = DissolutionFixtures.generateDissolutionData();
    private Company company = DissolutionFixtures.generateCompany();
    private CreatedBy createdBy = DissolutionFixtures.generateCreatedBy();
    private DissolutionSubmission submission = new DissolutionSubmission();
    private PaymentInformation paymentInformation = new PaymentInformation();
    private DissolutionVerdict verdict = new DissolutionVerdict();
    private boolean active = true;

    public static DissolutionTestDataBuilder aDissolution() {
        return new DissolutionTestDataBuilder();
    }

    public DissolutionTestDataBuilder withModifiedDateTime(LocalDateTime modifiedDateTime) {
        this.modifiedDateTime = modifiedDateTime;
        return this;
    }

    public DissolutionTestDataBuilder withData(DissolutionData data) {
        this.data = data;
        return this;
    }

    public DissolutionTestDataBuilder withCompany(Company company) {
        this.company = company;
        return this;
    }

    public DissolutionTestDataBuilder withCompanyNumber(String companyNumber) {
        if (company == null) {
            company = new Company();
        }
        company.setNumber(companyNumber);
        return this;
    }

    public DissolutionTestDataBuilder withCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public DissolutionTestDataBuilder withCreatedByEmail(String email) {
        if (createdBy == null) {
            createdBy = new CreatedBy();
        }
        createdBy.setEmail(email);
        return this;
    }

    public DissolutionTestDataBuilder withApplicationReference(String reference) {
        if (data.getApplication() == null) {
            data.setApplication(DissolutionFixtures.generateDissolutionApplication());
        }
        data.getApplication().setReference(reference);
        return this;
    }

    public DissolutionTestDataBuilder withDirectors(List<DissolutionDirector> directors) {
        data.setDirectors(directors);
        return this;
    }

    public DissolutionTestDataBuilder withDirectors(DissolutionDirectorTestDataBuilder... directorBuilders) {
        return withDirectors(Arrays.stream(directorBuilders).map(DissolutionDirectorTestDataBuilder::build).toList());
    }

    public DissolutionTestDataBuilder withDirector(DissolutionDirector director) {
        final List<DissolutionDirector> directors = data.getDirectors() == null
                ? new ArrayList<>()
                : new ArrayList<>(data.getDirectors());
        directors.add(director);
        data.setDirectors(directors);
        return this;
    }

    public DissolutionTestDataBuilder withDirector(DissolutionDirectorTestDataBuilder directorBuilder) {
        return withDirector(directorBuilder.build());
    }

    public DissolutionTestDataBuilder withOnlyDirector(DissolutionDirectorTestDataBuilder directorBuilder) {
        return withDirectors(Collections.singletonList(directorBuilder.build()));
    }

    public DissolutionTestDataBuilder withSubmission(DissolutionSubmission submission) {
        this.submission = submission;
        return this;
    }

    public DissolutionTestDataBuilder withPaymentInformation(PaymentInformation paymentInformation) {
        this.paymentInformation = paymentInformation;
        return this;
    }

    public DissolutionTestDataBuilder withVerdict(DissolutionVerdict verdict) {
        this.verdict = verdict;
        return this;
    }

    public DissolutionTestDataBuilder withActive(boolean active) {
        this.active = active;
        return this;
    }

    public Dissolution build() {
        final Dissolution dissolution = new Dissolution();
        dissolution.setModifiedDateTime(modifiedDateTime);
        dissolution.setData(data);
        dissolution.setCompany(company);
        dissolution.setCreatedBy(createdBy);
        dissolution.setSubmission(submission);
        dissolution.setPaymentInformation(paymentInformation);
        dissolution.setVerdict(verdict);
        dissolution.setActive(active);
        return dissolution;
    }
}

