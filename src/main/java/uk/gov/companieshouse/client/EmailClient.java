package uk.gov.companieshouse.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.SendEmail;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.exception.EmailSendException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.email.EmailDocument;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static java.lang.String.format;

@Component
@Qualifier("emailClient")
public class EmailClient {

    private final Supplier<InternalApiClient> internalApiClientSupplier;
    private final Logger logger;
    private final ObjectMapper objectMapper;
    private final String chsKafkaUrl;

    public EmailClient(@Value("${kafka.api.url}")String chsKafkaUrl,
                       Supplier<InternalApiClient> internalApiClientSupplier,
                       Logger logger,
                       ObjectMapper objectMapper) {
        this.chsKafkaUrl = chsKafkaUrl;
        this.internalApiClientSupplier = internalApiClientSupplier;
        this.logger = logger;
        this.objectMapper = objectMapper;
    }

    public <T> ApiResponse<Void> sendEmail(final EmailDocument<T> emailDocument) throws EmailSendException {
        try {
            var jsonData = objectMapper.writeValueAsString(emailDocument.getData());

            var sendEmail = new SendEmail();
            sendEmail.setAppId(emailDocument.getAppId());
            sendEmail.setMessageId(emailDocument.getMessageId());
            sendEmail.setMessageType(emailDocument.getMessageType());
            sendEmail.setJsonData(jsonData);
            sendEmail.setEmailAddress(emailDocument.getEmailAddress());

            var requestId = getRequestId().orElse(UUID.randomUUID().toString());

            var apiClient = internalApiClientSupplier.get();
            apiClient.setBasePath(chsKafkaUrl);
            apiClient.getHttpClient().setRequestId(requestId);

            var emailHandler = apiClient.sendEmailHandler();
            var emailPost = emailHandler.postSendEmail("/send-email", sendEmail);

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

    private Optional<String> getRequestId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(attributes.getRequest().getHeader("x-request-id"));
    }

}
