package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.model.dto.email.ApplicationAcceptedEmailData;
import uk.gov.companieshouse.model.dto.email.ApplicationRejectedEmailData;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.SignatoryToSignEmailData;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;

import java.util.List;

import static uk.gov.companieshouse.model.Constants.APPLICATION_ACCEPTED_EMAIL_SUBJECT;
import static uk.gov.companieshouse.model.Constants.APPLICATION_REJECTED_EMAIL_SUBJECT;
import static uk.gov.companieshouse.model.Constants.EMAIL_APP_ID;
import static uk.gov.companieshouse.model.Constants.EMAIL_TOPIC;
import static uk.gov.companieshouse.model.Constants.SIGNATORY_TO_SIGN_EMAIL_SUBJECT;
import static uk.gov.companieshouse.model.Constants.SUCCESSFUL_PAYMENT_EMAIL_SUBJECT;

public class EmailFixtures {

    public static final Object EMAIL_DATA = "some-email-data";
    public static final String EMAIL_ADDRESS = "user@email.com";
    public static final String MESSAGE_TYPE = "some-message-type";

    public static final String CHS_URL = "http://chs-url";
    public static final String CDN_HOST = "http://some-cdn-host";

    public static SuccessfulPaymentEmailData generateSuccessfulPaymentEmailData() {
        final SuccessfulPaymentEmailData successfulPaymentEmailData = new SuccessfulPaymentEmailData();

        successfulPaymentEmailData.setTo("user@mail.com");
        successfulPaymentEmailData.setSubject(SUCCESSFUL_PAYMENT_EMAIL_SUBJECT);
        successfulPaymentEmailData.setCdnHost(CDN_HOST);
        successfulPaymentEmailData.setDissolutionReferenceNumber("ABC123");
        successfulPaymentEmailData.setCompanyNumber("12345678");
        successfulPaymentEmailData.setCompanyName("Companies House");
        successfulPaymentEmailData.setChsUrl(CHS_URL);

        return successfulPaymentEmailData;
    }

    public static ApplicationAcceptedEmailData generateApplicationAcceptedEmailData() {
        final ApplicationAcceptedEmailData applicationAcceptedEmailData = new ApplicationAcceptedEmailData();

        applicationAcceptedEmailData.setTo("user@mail.com");
        applicationAcceptedEmailData.setSubject(APPLICATION_ACCEPTED_EMAIL_SUBJECT);
        applicationAcceptedEmailData.setCdnHost(CDN_HOST);
        applicationAcceptedEmailData.setDissolutionReferenceNumber("ABC123");
        applicationAcceptedEmailData.setCompanyNumber("12345678");
        applicationAcceptedEmailData.setCompanyName("Companies House");

        return applicationAcceptedEmailData;
    }

    public static ApplicationRejectedEmailData generateApplicationRejectedEmailData() {
        final ApplicationRejectedEmailData applicationRejectedEmailData = new ApplicationRejectedEmailData();

        applicationRejectedEmailData.setTo("user@mail.com");
        applicationRejectedEmailData.setSubject(APPLICATION_REJECTED_EMAIL_SUBJECT);
        applicationRejectedEmailData.setCdnHost(CDN_HOST);
        applicationRejectedEmailData.setDissolutionReferenceNumber("ABC123");
        applicationRejectedEmailData.setCompanyNumber("12345678");
        applicationRejectedEmailData.setCompanyName("Companies House");
        applicationRejectedEmailData.setRejectReasons(List.of(
                "Dissolution application reject reason 1",
                "Dissolution application reject reason 2",
                "Dissolution application reject reason 3"
        ));

        return applicationRejectedEmailData;
    }

    public static SignatoryToSignEmailData generateSignatoryToSignEmailData() {
        final SignatoryToSignEmailData emailData = new SignatoryToSignEmailData();

        emailData.setTo("user@mail.com");
        emailData.setSubject(SIGNATORY_TO_SIGN_EMAIL_SUBJECT);
        emailData.setCdnHost(CDN_HOST);
        emailData.setDissolutionReferenceNumber("ABC123");
        emailData.setCompanyNumber("12345678");
        emailData.setCompanyName("Companies House");
        emailData.setDissolutionDeadlineDate("17 September 2020");
        emailData.setChsUrl(CHS_URL);

        return emailData;
    }

    public static <T> EmailDocument<T> generateEmailDocument(T data) {
        final EmailDocument<T> emailDocument = new EmailDocument<>();

        emailDocument.setAppId(EMAIL_APP_ID);
        emailDocument.setMessageId("some-uuid");
        emailDocument.setMessageType(MESSAGE_TYPE);
        emailDocument.setData(data);
        emailDocument.setEmailAddress(EMAIL_ADDRESS);
        emailDocument.setCreatedAt("2020-06-15T08:30:00.000");
        emailDocument.setTopic(EMAIL_TOPIC);

        return emailDocument;
    }

    public static Message generateKafkaMessage(byte[] value) {
        final Message message = new Message();

        message.setTopic(EMAIL_TOPIC);
        message.setTimestamp(1597047606L);
        message.setValue(value);

        return message;
    }
}
