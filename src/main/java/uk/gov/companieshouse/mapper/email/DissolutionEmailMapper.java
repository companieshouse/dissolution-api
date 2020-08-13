package uk.gov.companieshouse.mapper.email;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;

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
}
