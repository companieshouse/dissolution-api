package uk.gov.companieshouse.mapper.email;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.email.ApplicationAcceptedEmailData;
import uk.gov.companieshouse.model.dto.email.ApplicationRejectedEmailData;
import uk.gov.companieshouse.model.dto.email.SignatoryToSignEmailData;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;

import java.util.List;

import static uk.gov.companieshouse.model.Constants.APPLICATION_ACCEPTED_EMAIL_SUBJECT;
import static uk.gov.companieshouse.model.Constants.APPLICATION_REJECTED_EMAIL_SUBJECT;
import static uk.gov.companieshouse.model.Constants.SIGNATORY_TO_SIGN_EMAIL_SUBJECT;
import static uk.gov.companieshouse.model.Constants.SUCCESSFUL_PAYMENT_EMAIL_SUBJECT;

@Service
public class DissolutionEmailMapper {

    final private EnvironmentConfig environmentConfig;

    public DissolutionEmailMapper(EnvironmentConfig environmentConfig) {
        this.environmentConfig = environmentConfig;
    }

    public SuccessfulPaymentEmailData mapToSuccessfulPaymentEmailData(Dissolution dissolution) {
        SuccessfulPaymentEmailData successfulPaymentEmailData = new SuccessfulPaymentEmailData();

        successfulPaymentEmailData.setTo(dissolution.getCreatedBy().getEmail());
        successfulPaymentEmailData.setSubject(SUCCESSFUL_PAYMENT_EMAIL_SUBJECT);
        successfulPaymentEmailData.setCdnHost(environmentConfig.getCdnHost());
        successfulPaymentEmailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        successfulPaymentEmailData.setCompanyNumber(dissolution.getCompany().getNumber());
        successfulPaymentEmailData.setCompanyName(dissolution.getCompany().getName());
        successfulPaymentEmailData.setChsUrl(environmentConfig.getChsUrl());

        return successfulPaymentEmailData;
    }

    public ApplicationAcceptedEmailData mapToApplicationAcceptedEmailData(Dissolution dissolution) {
        ApplicationAcceptedEmailData applicationAcceptedEmailData = new ApplicationAcceptedEmailData();

        applicationAcceptedEmailData.setTo(dissolution.getCreatedBy().getEmail());
        applicationAcceptedEmailData.setSubject(APPLICATION_ACCEPTED_EMAIL_SUBJECT);
        applicationAcceptedEmailData.setCdnHost(environmentConfig.getCdnHost());
        applicationAcceptedEmailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        applicationAcceptedEmailData.setCompanyNumber(dissolution.getCompany().getNumber());
        applicationAcceptedEmailData.setCompanyName(dissolution.getCompany().getName());

        return applicationAcceptedEmailData;
    }

    public ApplicationRejectedEmailData mapToApplicationRejectedEmailData(
            Dissolution dissolution, List<String> rejectReasons
    ) {
        ApplicationRejectedEmailData applicationRejectedEmailData = new ApplicationRejectedEmailData();

        applicationRejectedEmailData.setTo(dissolution.getCreatedBy().getEmail());
        applicationRejectedEmailData.setSubject(APPLICATION_REJECTED_EMAIL_SUBJECT);
        applicationRejectedEmailData.setCdnHost(environmentConfig.getCdnHost());
        applicationRejectedEmailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        applicationRejectedEmailData.setCompanyNumber(dissolution.getCompany().getNumber());
        applicationRejectedEmailData.setCompanyName(dissolution.getCompany().getName());
        applicationRejectedEmailData.setRejectReasons(rejectReasons);

        return applicationRejectedEmailData;
    }

    public SignatoryToSignEmailData mapToSignatoryToSignEmailData(Dissolution dissolution, String signatoryEmail, String deadline) {
        SignatoryToSignEmailData signatoryToSignEmailData = new SignatoryToSignEmailData();

        signatoryToSignEmailData.setTo(signatoryEmail);
        signatoryToSignEmailData.setSubject(SIGNATORY_TO_SIGN_EMAIL_SUBJECT);
        signatoryToSignEmailData.setCdnHost(environmentConfig.getCdnHost());
        signatoryToSignEmailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        signatoryToSignEmailData.setCompanyNumber(dissolution.getCompany().getNumber());
        signatoryToSignEmailData.setCompanyName(dissolution.getCompany().getName());
        signatoryToSignEmailData.setDissolutionDeadlineDate(deadline);
        signatoryToSignEmailData.setChsUrl(environmentConfig.getChsUrl());

        return signatoryToSignEmailData;
    }
}
