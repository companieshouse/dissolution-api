package uk.gov.companieshouse.mapper.email;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.email.SignatoryToSignEmailData;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;

import static uk.gov.companieshouse.model.Constants.SIGNATORY_TO_SIGN_EMAIL_SUBJECT;
import static uk.gov.companieshouse.model.Constants.SUCCESSFUL_PAYMENT_EMAIL_SUBJECT;

@Service
public class DissolutionEmailMapper {

    final private EnvironmentConfig environmentConfig;

    public DissolutionEmailMapper(EnvironmentConfig environmentConfig) {
        this.environmentConfig = environmentConfig;
    }

    public SuccessfulPaymentEmailData mapToSuccessfulPaymentEmailData(Dissolution dissolution) {
        final String applicantEmailAddress = dissolution.getCreatedBy().getEmail();
        final String dissolutionReferenceNumber = dissolution.getData().getApplication().getReference();
        final String companyNumber = dissolution.getCompany().getNumber();
        final String companyName = dissolution.getCompany().getName();

        SuccessfulPaymentEmailData successfulPaymentEmailData = new SuccessfulPaymentEmailData();

        successfulPaymentEmailData.setTo(applicantEmailAddress);
        successfulPaymentEmailData.setSubject(SUCCESSFUL_PAYMENT_EMAIL_SUBJECT);
        successfulPaymentEmailData.setDissolutionReferenceNumber(dissolutionReferenceNumber);
        successfulPaymentEmailData.setCompanyNumber(companyNumber);
        successfulPaymentEmailData.setCompanyName(companyName);
        successfulPaymentEmailData.setChsUrl(environmentConfig.getChsUrl());
        successfulPaymentEmailData.setCdnHost(environmentConfig.getCdnHost());

        return successfulPaymentEmailData;
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
