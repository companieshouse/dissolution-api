package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.email.DissolutionEmailMapper;
import uk.gov.companieshouse.mapper.email.EmailMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.SignatoryToSignEmailData;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;
import uk.gov.companieshouse.service.email.EmailService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.companieshouse.model.Constants.SIGNATORY_TO_SIGN_MESSAGE_TYPE;
import static uk.gov.companieshouse.model.Constants.SUCCESSFUL_PAYMENT_MESSAGE_TYPE;

@Service
public class DissolutionEmailService {

    private final DissolutionEmailMapper dissolutionEmailMapper;
    private final EmailMapper emailMapper;
    private final EmailService emailService;
    private final DissolutionDeadlineDateCalculator deadlineDateCalculator;

    @Autowired
    public DissolutionEmailService(
            DissolutionEmailMapper dissolutionEmailMapper,
            EmailMapper emailMapper,
            EmailService emailService,
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

        emailService.sendMessage(emailDocument);
    }

    public void notifySignatoriesToSign(Dissolution dissolution) {
        final String deadlineDate = deadlineDateCalculator.calculateSignatoryDeadlineDate(LocalDateTime.now());

        getUniqueSignatories(dissolution)
                .stream()
                .map(signatoryEmail -> mapToSignatoryToSignEmail(dissolution, signatoryEmail, deadlineDate))
                .forEach(emailService::sendMessage);
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
