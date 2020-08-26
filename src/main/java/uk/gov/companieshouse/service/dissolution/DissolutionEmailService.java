package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.email.DissolutionEmailMapper;
import uk.gov.companieshouse.mapper.email.EmailMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionRejectReason;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.dto.email.ApplicationAcceptedEmailData;
import uk.gov.companieshouse.model.dto.email.ApplicationRejectedEmailData;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.SignatoryToSignEmailData;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;
import uk.gov.companieshouse.model.enums.VerdictResult;
import uk.gov.companieshouse.service.email.EmailService;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.companieshouse.model.Constants.APPLICATION_ACCEPTED_MESSAGE_TYPE;
import static uk.gov.companieshouse.model.Constants.APPLICATION_REJECTED_MESSAGE_TYPE;
import java.time.LocalDateTime;

import static uk.gov.companieshouse.model.Constants.SIGNATORY_TO_SIGN_MESSAGE_TYPE;
import static uk.gov.companieshouse.model.Constants.SUCCESSFUL_PAYMENT_MESSAGE_TYPE;

@Service
public class DissolutionEmailService {

    private final DissolutionEmailMapper dissolutionEmailMapper;
    private final EmailMapper emailMapper;
    private final EmailService emailService;
    private final DissolutionDeadlineDateCalculator deadlineDateCalculator;

    @Autowired
    public DissolutionEmailService(DissolutionEmailMapper dissolutionEmailMapper, EmailMapper emailMapper, EmailService emailService,
            DissolutionDeadlineDateCalculator deadlineDateCalculator
    ) {
        this.dissolutionEmailMapper = dissolutionEmailMapper;
        this.emailMapper = emailMapper;
        this.emailService = emailService;
        this.deadlineDateCalculator = deadlineDateCalculator;
    }

    public void sendSuccessfulPaymentEmail(Dissolution dissolution) {
        final SuccessfulPaymentEmailData successfulPaymentEmailData =
                this.dissolutionEmailMapper.mapToSuccessfulPaymentEmailData(dissolution);

        final EmailDocument<SuccessfulPaymentEmailData> emailDocument = this.emailMapper.mapToEmailDocument(
                successfulPaymentEmailData, successfulPaymentEmailData.getTo(), SUCCESSFUL_PAYMENT_MESSAGE_TYPE
        );

        sendEmail(emailDocument);
    }

    public void sendApplicationOutcomeEmail(Dissolution dissolution, DissolutionVerdict dissolutionVerdict) {
        EmailDocument<?> emailDocument = dissolutionVerdict.getResult() == VerdictResult.ACCEPTED ?
                this.getApplicationAcceptedEmailDocument(dissolution) :
                this.getApplicationRejectedEmailDocument(dissolution, dissolutionVerdict);

        sendEmail(emailDocument);
    }

    public void notifySignatoriesToSign(Dissolution dissolution) {
        final String deadlineDate = deadlineDateCalculator.calculateSignatoryDeadlineDate(LocalDateTime.now());

        getUniqueSignatories(dissolution)
                .stream()
                .map(signatoryEmail -> mapToSignatoryToSignEmail(dissolution, signatoryEmail, deadlineDate))
                .forEach(emailService::sendMessage);
    }

    private EmailDocument<ApplicationAcceptedEmailData> getApplicationAcceptedEmailDocument(Dissolution dissolution) {
        final ApplicationAcceptedEmailData applicationAcceptedEmailData =
                this.dissolutionEmailMapper.mapToApplicationAcceptedEmailData(dissolution);

        return this.emailMapper.mapToEmailDocument(
                applicationAcceptedEmailData, applicationAcceptedEmailData.getTo(), APPLICATION_ACCEPTED_MESSAGE_TYPE
        );
    }

    private EmailDocument<ApplicationRejectedEmailData> getApplicationRejectedEmailDocument(Dissolution dissolution, DissolutionVerdict dissolutionVerdict) {
        List<String> rejectReasonsAsStrings = dissolutionVerdict.getRejectReasons().stream().map(DissolutionRejectReason::getTextEnglish).collect(Collectors.toList());

        final ApplicationRejectedEmailData applicationRejectedEmailData =
                this.dissolutionEmailMapper.mapToApplicationRejectedEmailData(dissolution, rejectReasonsAsStrings);

        return this.emailMapper.mapToEmailDocument(
                applicationRejectedEmailData, applicationRejectedEmailData.getTo(), APPLICATION_REJECTED_MESSAGE_TYPE
        );
    }

    private <T> void sendEmail(EmailDocument<T> emailDocument) {
        emailService.sendMessage(emailDocument);
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

    private EmailDocument<SignatoryToSignEmailData> mapToSignatoryToSignEmail(Dissolution dissolution, String signatoryEmail, String deadlineDate) {
        final SignatoryToSignEmailData emailData = dissolutionEmailMapper.mapToSignatoryToSignEmailData(dissolution, signatoryEmail, deadlineDate);
        return this.emailMapper.mapToEmailDocument(emailData, signatoryEmail, SIGNATORY_TO_SIGN_MESSAGE_TYPE);
    }
}
