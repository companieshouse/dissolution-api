package uk.gov.companieshouse.mapper.email;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.email.ApplicationAcceptedEmailData;
import uk.gov.companieshouse.model.dto.email.ApplicationRejectedEmailData;
import uk.gov.companieshouse.model.dto.email.PendingPaymentEmailData;
import uk.gov.companieshouse.model.dto.email.SignatoryToSignEmailData;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;

import java.util.List;

import static uk.gov.companieshouse.model.Constants.*;

@Service
public class DissolutionEmailMapper {

    private final EnvironmentConfig environmentConfig;

    public DissolutionEmailMapper(EnvironmentConfig environmentConfig) {
        this.environmentConfig = environmentConfig;
    }

    public SuccessfulPaymentEmailData mapToSuccessfulPaymentEmailData(Dissolution dissolution, String signatoryEmail) {
        SuccessfulPaymentEmailData successfulPaymentEmailData = new SuccessfulPaymentEmailData();

        successfulPaymentEmailData.setTo(signatoryEmail);
        successfulPaymentEmailData.setSubject(SUCCESSFUL_PAYMENT_EMAIL_SUBJECT);
        successfulPaymentEmailData.setCdnHost(environmentConfig.getCdnHost());
        successfulPaymentEmailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        successfulPaymentEmailData.setCompanyNumber(dissolution.getCompany().getNumber());
        successfulPaymentEmailData.setCompanyName(dissolution.getCompany().getName());
        successfulPaymentEmailData.setChsUrl(environmentConfig.getChsUrl());
        successfulPaymentEmailData.setPaymentReference(dissolution.getPaymentInformation().getReference());

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
        applicationAcceptedEmailData.setPaymentReference(dissolution.getPaymentInformation().getReference());

        return applicationAcceptedEmailData;
    }

    public ApplicationRejectedEmailData mapToApplicationRejectedEmailData(
            Dissolution dissolution, List<String> rejectReasons, String email
    ) {
        ApplicationRejectedEmailData applicationRejectedEmailData = new ApplicationRejectedEmailData();

        applicationRejectedEmailData.setTo(email);
        applicationRejectedEmailData.setSubject(APPLICATION_REJECTED_EMAIL_SUBJECT);
        applicationRejectedEmailData.setCdnHost(environmentConfig.getCdnHost());
        applicationRejectedEmailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        applicationRejectedEmailData.setCompanyNumber(dissolution.getCompany().getNumber());
        applicationRejectedEmailData.setCompanyName(dissolution.getCompany().getName());
        applicationRejectedEmailData.setRejectReasons(rejectReasons);
        applicationRejectedEmailData.setPaymentReference(dissolution.getPaymentInformation().getReference());

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

    public PendingPaymentEmailData mapToPendingPaymentEmailData(Dissolution dissolution) {
        PendingPaymentEmailData pendingPaymentEmailData = new PendingPaymentEmailData();

        pendingPaymentEmailData.setTo(dissolution.getCreatedBy().getEmail());
        pendingPaymentEmailData.setSubject(PENDING_PAYMENT_EMAIL_SUBJECT);
        pendingPaymentEmailData.setDissolutionReferenceNumber(dissolution.getData().getApplication().getReference());
        pendingPaymentEmailData.setCompanyNumber(dissolution.getCompany().getNumber());
        pendingPaymentEmailData.setCompanyName(dissolution.getCompany().getName());
        pendingPaymentEmailData.setChsUrl(environmentConfig.getChsUrl());
        pendingPaymentEmailData.setCdnHost(environmentConfig.getCdnHost());

        return pendingPaymentEmailData;
    }
}
