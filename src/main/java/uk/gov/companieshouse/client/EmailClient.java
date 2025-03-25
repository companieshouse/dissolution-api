package uk.gov.companieshouse.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.SendEmail;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateSendEmailHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateSendEmailPost;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.exception.EmailSendException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

import static java.lang.String.format;

@Component
@Qualifier("emailClient")
public class EmailClient {

    private final InternalApiClient internalApiClient;
    private final Logger logger;
    private final ObjectMapper objectMapper;

    public EmailClient(InternalApiClient internalApiClient, Logger logger, ObjectMapper objectMapper) {
        this.internalApiClient = internalApiClient;
        this.logger = logger;
        this.objectMapper = objectMapper;
    }

    public <T> ApiResponse<Void> sendEmail(final EmailDocument<T> emailDocument) throws EmailSendException {
        try {
            String jsonData = objectMapper.writeValueAsString(emailDocument.getData());

            SendEmail sendEmail = new SendEmail();
            sendEmail.setAppId(emailDocument.getAppId());
            sendEmail.setMessageId(emailDocument.getMessageId());
            sendEmail.setMessageType(emailDocument.getMessageType());
            sendEmail.setJsonData(jsonData);
            sendEmail.setEmailAddress(emailDocument.getEmailAddress());

            PrivateSendEmailHandler emailHandler = internalApiClient.sendEmailHandler();
            PrivateSendEmailPost emailPost = emailHandler.postSendEmail("/send-email", sendEmail);

            ApiResponse<Void> response = emailPost.execute();

            logger.info(format("Posted '%s' email to CHS Kafka API: Response %d",
                sendEmail.getMessageType(), response.getStatusCode()));

            return response;

        } catch(JsonProcessingException ex) {
            logger.error("Error creating payload", ex);
            throw new EmailSendException(ex.getMessage());

        } catch (ApiErrorResponseException ex) {
            logger.error("Error sending email", ex);
            throw new EmailSendException(ex.getMessage());
        }
    }

}
