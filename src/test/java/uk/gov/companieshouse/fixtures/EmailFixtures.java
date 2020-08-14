package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;

import static uk.gov.companieshouse.model.Constants.EMAIL_APP_ID;
import static uk.gov.companieshouse.model.Constants.EMAIL_TOPIC;
import static uk.gov.companieshouse.model.Constants.SUCCESSFUL_PAYMENT_EMAIL_SUBJECT;

public class EmailFixtures {

    public static final Object EMAIL_DATA = "some-email-data";
    public static final String EMAIL_ADDRESS = "user@email.com";
    public static final String MESSAGE_TYPE = "some-message-type";

    public static final String CDN_URL = "http://chs-url";
    public static final String CDN_HOST = "http://some-cdn-host";

    public static SuccessfulPaymentEmailData generateSuccessfulPaymentEmailData() {
        final SuccessfulPaymentEmailData successfulPaymentEmailData = new SuccessfulPaymentEmailData();

        successfulPaymentEmailData.setTo("user@mail.com");
        successfulPaymentEmailData.setSubject(SUCCESSFUL_PAYMENT_EMAIL_SUBJECT);
        successfulPaymentEmailData.setDissolutionReferenceNumber("ABC123");
        successfulPaymentEmailData.setCompanyNumber("12345678");
        successfulPaymentEmailData.setCompanyName("Companies House");
        successfulPaymentEmailData.setChsUrl(CDN_URL);
        successfulPaymentEmailData.setCdnHost(CDN_HOST);

        return successfulPaymentEmailData;
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
