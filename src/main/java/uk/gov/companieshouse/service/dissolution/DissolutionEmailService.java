package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.mapper.email.DissolutionEmailMapper;
import uk.gov.companieshouse.mapper.email.EmailMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.dissolution.DissolutionRejectReason;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.dto.email.ApplicationAcceptedEmailData;
import uk.gov.companieshouse.model.dto.email.ApplicationRejectedEmailData;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.MessageType;
import uk.gov.companieshouse.model.dto.email.PendingPaymentEmailData;
import uk.gov.companieshouse.model.dto.email.SignatoryToSignEmailData;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;
import uk.gov.companieshouse.model.enums.VerdictResult;
import uk.gov.companieshouse.service.email.EmailService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.companieshouse.util.DateTimeGenerator.generateCurrentDateTime;

@Service
public class DissolutionEmailService {

    private final DissolutionEmailMapper dissolutionEmailMapper;
    private final DissolutionMessageTypeCalculator messageTypeCalculator;
    private final EmailMapper emailMapper;
    private final EmailService emailService;
    private final DissolutionDeadlineDateCalculator deadlineDateCalculator;
    private final EnvironmentConfig environmentConfig;

    @Autowired
    public DissolutionEmailService(
            DissolutionEmailMapper dissolutionEmailMapper,
            DissolutionMessageTypeCalculator messageTypeCalculator,
            EmailMapper emailMapper,
            EmailService emailService,
            DissolutionDeadlineDateCalculator deadlineDateCalculator,
            EnvironmentConfig environmentConfig
    ) {
        this.dissolutionEmailMapper = dissolutionEmailMapper;
        this.messageTypeCalculator = messageTypeCalculator;
        this.emailMapper = emailMapper;
        this.emailService = emailService;
        this.deadlineDateCalculator = deadlineDateCalculator;
        this.environmentConfig = environmentConfig;
    }

    public void sendSuccessfulPaymentEmail(Dissolution dissolution) {
        final SuccessfulPaymentEmailData emailData = this.dissolutionEmailMapper.mapToSuccessfulPaymentEmailData(dissolution);

        final MessageType messageType = messageTypeCalculator.getForSuccessfulPayment(dissolution);

        final EmailDocument<SuccessfulPaymentEmailData> emailDocument = this.emailMapper.mapToEmailDocument(
                emailData, emailData.getTo(), messageType
        );

        sendEmail(emailDocument);
    }

    public void sendApplicationOutcomeEmail(Dissolution dissolution, DissolutionVerdict dissolutionVerdict) {
        EmailDocument<?> emailDocument = dissolutionVerdict.getResult() == VerdictResult.ACCEPTED ?
                this.getApplicationAcceptedEmailDocument(dissolution) :
                this.getApplicationRejectedEmailDocument(dissolution, dissolutionVerdict);

        sendEmail(emailDocument);
    }
    
    public void sendRejectionEmailToFinance(Dissolution dissolution, DissolutionVerdict dissolutionVerdict) {
        EmailDocument<?> emailDocument = this.getApplicationRejectedEmailDocument(dissolution, dissolutionVerdict);
        emailDocument.setEmailAddress(environmentConfig.getChsFinanceEmail());
        
        sendEmail(emailDocument);
    }

    public void sendPendingPaymentEmail(Dissolution dissolution) {
        final PendingPaymentEmailData emailData = this.dissolutionEmailMapper.mapToPendingPaymentEmailData(dissolution);

        final MessageType messageType = messageTypeCalculator.getForPendingPayment(dissolution);

        final EmailDocument<PendingPaymentEmailData> emailDocument = this.emailMapper.mapToEmailDocument(
                emailData, emailData.getTo(), messageType
        );

        sendEmail(emailDocument);
    }

    private EmailDocument<ApplicationAcceptedEmailData> getApplicationAcceptedEmailDocument(Dissolution dissolution) {
        final ApplicationAcceptedEmailData emailData = this.dissolutionEmailMapper.mapToApplicationAcceptedEmailData(dissolution);

        final MessageType messageType = messageTypeCalculator.getForApplicationAccepted(dissolution);

        return this.emailMapper.mapToEmailDocument(
                emailData, emailData.getTo(), messageType
        );
    }

    private EmailDocument<ApplicationRejectedEmailData> getApplicationRejectedEmailDocument(Dissolution dissolution, DissolutionVerdict dissolutionVerdict) {
        List<String> rejectReasonsAsStrings = dissolutionVerdict.getRejectReasons().stream().map(DissolutionRejectReason::getTextEnglish).collect(Collectors.toList());

        final ApplicationRejectedEmailData emailData = this.dissolutionEmailMapper.mapToApplicationRejectedEmailData(dissolution, rejectReasonsAsStrings);

        final MessageType messageType = messageTypeCalculator.getForApplicationRejected(dissolution);

        return this.emailMapper.mapToEmailDocument(
                emailData, emailData.getTo(), messageType
        );
    }

    public void notifySignatoriesToSign(Dissolution dissolution) {
        final MessageType messageType = messageTypeCalculator.getForSignatoriesToSign(dissolution);

        final String deadlineDate = deadlineDateCalculator.calculateSignatoryDeadlineDate(generateCurrentDateTime());

        getUniqueSignatories(dissolution)
                .stream()
                .filter(signatoryEmail -> !signatoryEmail.equals(dissolution.getCreatedBy().getEmail()))
                .map(signatoryEmail -> mapToSignatoryToSignEmail(dissolution, signatoryEmail, messageType, deadlineDate))
                .forEach(this::sendEmail);
    }

    private List<String> getUniqueSignatories(Dissolution dissolution) {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .map(DissolutionDirector::getEmail)
                .distinct()
                .collect(Collectors.toList());
    }

    private EmailDocument<SignatoryToSignEmailData> mapToSignatoryToSignEmail(Dissolution dissolution, String signatoryEmail, MessageType messageType, String deadlineDate) {
        final SignatoryToSignEmailData emailData = dissolutionEmailMapper.mapToSignatoryToSignEmailData(dissolution, signatoryEmail, deadlineDate);
        return this.emailMapper.mapToEmailDocument(emailData, signatoryEmail, messageType);
    }

    private <T> void sendEmail(EmailDocument<T> emailDocument) {
        emailService.sendMessage(emailDocument);
    }
}
