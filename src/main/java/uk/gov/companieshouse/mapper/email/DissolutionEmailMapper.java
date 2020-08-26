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
        successfulPaymentEmailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        successfulPaymentEmailData.setCompanyNumber(dissolution.getCompany().getNumber());
        successfulPaymentEmailData.setCompanyName(dissolution.getCompany().getName());
        successfulPaymentEmailData.setChsUrl(environmentConfig.getChsUrl());
        successfulPaymentEmailData.setCdnHost(environmentConfig.getCdnHost());

        return successfulPaymentEmailData;
    }

    public ApplicationAcceptedEmailData mapToApplicationAcceptedEmailData(Dissolution dissolution) {
        ApplicationAcceptedEmailData applicationAcceptedEmailData = new ApplicationAcceptedEmailData();

        applicationAcceptedEmailData.setTo(dissolution.getCreatedBy().getEmail());
        applicationAcceptedEmailData.setSubject(APPLICATION_ACCEPTED_EMAIL_SUBJECT);
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
        applicationRejectedEmailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        applicationRejectedEmailData.setCompanyNumber(dissolution.getCompany().getNumber());
        applicationRejectedEmailData.setCompanyName(dissolution.getCompany().getName());
        applicationRejectedEmailData.setRejectReasons(rejectReasons);

        return applicationRejectedEmailData;
    }

    public SignatoryToSignEmailData mapToSignatoryToSignEmailData(Dissolution dissolution, String signatoryEmail, String deadline) {
        SignatoryToSignEmailData emailData = new SignatoryToSignEmailData();

        emailData.setTo(signatoryEmail);
        emailData.setSubject(SIGNATORY_TO_SIGN_EMAIL_SUBJECT);
        emailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        emailData.setCompanyNumber(dissolution.getCompany().getNumber());
        emailData.setCompanyName(dissolution.getCompany().getName());
        emailData.setDissolutionDeadlineDate(deadline);
        emailData.setChsUrl(environmentConfig.getChsUrl());
        emailData.setCdnHost(environmentConfig.getCdnHost());

        return emailData;
    }
}
