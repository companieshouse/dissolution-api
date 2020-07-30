package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.SuccessfulPaymentEmailConfig;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;
import uk.gov.companieshouse.util.DateTimeGenerator;
import uk.gov.companieshouse.util.UUIDGenerator;

@Service
public class DissolutionEmailMapper {

    final private SuccessfulPaymentEmailConfig successfulPaymentEmailConfig;

    public DissolutionEmailMapper(SuccessfulPaymentEmailConfig successfulPaymentEmailConfig) {
        this.successfulPaymentEmailConfig = successfulPaymentEmailConfig;
    }

    public EmailDocument<SuccessfulPaymentEmailData> mapToEmailDocument(
        String dissolutionReferenceNumber, String companyNumber, String companyName, String emailAddress
    ) {
        EmailDocument<SuccessfulPaymentEmailData> emailDocument = new EmailDocument<>();

        emailDocument.setAppId(successfulPaymentEmailConfig.getAppId());
        emailDocument.setMessageId(UUIDGenerator.generateUUID());
        emailDocument.setMessageType(successfulPaymentEmailConfig.getMessageType());
        emailDocument.setData(
            mapToSuccessfulPaymentEmailData(
                dissolutionReferenceNumber, companyNumber, companyName, emailAddress
            )
        );
        emailDocument.setEmailAddress(emailAddress);
        emailDocument.setCreatedAt(DateTimeGenerator.generateCurrentDateTime().toString());
        emailDocument.setTopic(successfulPaymentEmailConfig.getTopic());

        return emailDocument;
    }

    private SuccessfulPaymentEmailData mapToSuccessfulPaymentEmailData(
        String dissolutionReferenceNumber, String companyNumber, String companyName, String emailAddress
    ) {
        SuccessfulPaymentEmailData successfulPaymentEmailData = new SuccessfulPaymentEmailData();

        successfulPaymentEmailData.setTo(emailAddress);
        successfulPaymentEmailData.setSubject(successfulPaymentEmailConfig.getSubject());
        successfulPaymentEmailData.setDissolutionReferenceNumber(dissolutionReferenceNumber);
        successfulPaymentEmailData.setCompanyNumber(companyNumber);
        successfulPaymentEmailData.setCompanyName(companyName);
        successfulPaymentEmailData.setEnv("http://chs.local/"); // TODO: Pass environment domain name as environment variable here

        return successfulPaymentEmailData;
    }
}
