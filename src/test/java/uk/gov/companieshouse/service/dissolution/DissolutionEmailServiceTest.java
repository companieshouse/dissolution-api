package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.config.EnvironmentConfig;
import uk.gov.companieshouse.fixtures.EmailFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.email.DissolutionEmailMapper;
import uk.gov.companieshouse.mapper.email.EmailMapper;
import uk.gov.companieshouse.model.db.dissolution.CreatedBy;
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
import uk.gov.companieshouse.model.dto.email.SupportNotificationEmailData;
import uk.gov.companieshouse.model.enums.VerdictResult;
import uk.gov.companieshouse.service.email.EmailService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.*;
import static uk.gov.companieshouse.fixtures.EmailFixtures.generateEmailDocument;
import static uk.gov.companieshouse.fixtures.EmailFixtures.generateSignatoryToSignEmailData;
import static uk.gov.companieshouse.fixtures.EmailFixtures.generateSupportNotificationEmailData;

@ExtendWith(MockitoExtension.class)
public class DissolutionEmailServiceTest {

    private static final String SIGNATORY_TO_SIGN_DEADLINE = "17 September 2020";
    private static final String SIGNATORY_EMAIL_ONE = "signatory1@mail.com";
    private static final String SIGNATORY_EMAIL_TWO = "signatory2@mail.com";
    private static final String APPLICANT_EMAIL = "applicant@mail.com";
    private static final String DIRECTOR_EMAIL = "john@doe.com";

    @InjectMocks
    private DissolutionEmailService dissolutionEmailService;

    @Mock
    private DissolutionEmailMapper dissolutionEmailMapper;

    @Mock
    private DissolutionMessageTypeCalculator messageTypeCalculator;

    @Mock
    private EmailMapper emailMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private EnvironmentConfig environmentConfig;

    @Mock
    private DissolutionDeadlineDateCalculator deadlineDateCalculator;

    @Mock
    private Logger logger;

    @Test
    void sendSuccessfulPaymentEmail_shouldGenerateAndSendASuccessfulPaymentEmail() {
        final Dissolution dissolution = generateDissolution();
        final SuccessfulPaymentEmailData emailData = EmailFixtures.generateSuccessfulPaymentEmailData();
        final SuccessfulPaymentEmailData directorEmailData = EmailFixtures.generateSuccessfulPaymentEmailDataForDirector();
        final EmailDocument<SuccessfulPaymentEmailData> emailDocument = generateEmailDocument(emailData);
        final EmailDocument<SuccessfulPaymentEmailData> directorEmailDocument = generateEmailDocument(directorEmailData);

        when(dissolutionEmailMapper.mapToSuccessfulPaymentEmailData(dissolution,dissolution.getCreatedBy().getEmail())).thenReturn(emailData);
        when(dissolutionEmailMapper.mapToSuccessfulPaymentEmailData(dissolution,DIRECTOR_EMAIL)).thenReturn(directorEmailData);
        when(messageTypeCalculator.getForSuccessfulPayment(dissolution)).thenReturn(MessageType.SUCCESSFUL_PAYMENT);
        when(emailMapper.mapToEmailDocument(emailData, emailData.getTo(), MessageType.SUCCESSFUL_PAYMENT)).thenReturn(emailDocument);
        when(emailMapper.mapToEmailDocument(directorEmailData, directorEmailData.getTo(), MessageType.SUCCESSFUL_PAYMENT)).thenReturn(directorEmailDocument);

        dissolutionEmailService.sendSuccessfulPaymentEmail(dissolution);

        verify(emailMapper).mapToEmailDocument(emailData, emailData.getTo(), MessageType.SUCCESSFUL_PAYMENT);
        verify(emailMapper).mapToEmailDocument(directorEmailData, directorEmailData.getTo(), MessageType.SUCCESSFUL_PAYMENT);
        verify(emailService).sendMessage(emailDocument);
        verify(emailService).sendMessage(directorEmailDocument);
        verify(emailService, times(2)).sendMessage(any());
    }

    @Test
    void sendPendingPaymentEmail_shouldGenerateAndSendAPendingPaymentEmail() {
        final Dissolution dissolution = generateDissolution();
        final PendingPaymentEmailData emailData = EmailFixtures.generatePendingPaymentEmailData();
        final EmailDocument<PendingPaymentEmailData> emailDocument = generateEmailDocument(emailData);

        when(dissolutionEmailMapper.mapToPendingPaymentEmailData(dissolution)).thenReturn(emailData);
        when(messageTypeCalculator.getForPendingPayment(dissolution)).thenReturn(MessageType.PENDING_PAYMENT);
        when(emailMapper.mapToEmailDocument(emailData, emailData.getTo(), MessageType.PENDING_PAYMENT)).thenReturn(emailDocument);

        dissolutionEmailService.sendPendingPaymentEmail(dissolution);

        verify(emailMapper).mapToEmailDocument(emailData, emailData.getTo(), MessageType.PENDING_PAYMENT);
        verify(emailService).sendMessage(emailDocument);
    }

    @Test
    void sendApplicationOutcomeEmail_shouldGenerateAndSendAnApplicationAcceptedEmailToAllDirectors() {
        final Dissolution dissolution = generateDissolution();
        final DissolutionVerdict dissolutionVerdict = generateDissolutionVerdict();

        final ApplicationAcceptedEmailData emailData = EmailFixtures.generateApplicationAcceptedEmailData();
        final EmailDocument<ApplicationAcceptedEmailData> emailDocument = generateEmailDocument(emailData);

        when(dissolutionEmailMapper.mapToApplicationAcceptedEmailData(dissolution)).thenReturn(emailData);
        when(messageTypeCalculator.getForApplicationAccepted(dissolution)).thenReturn(MessageType.APPLICATION_ACCEPTED);
        when(emailMapper.mapToEmailDocument(emailData, emailData.getTo(), MessageType.APPLICATION_ACCEPTED)).thenReturn(emailDocument);

        dissolutionEmailService.sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);

        verify(emailService).sendMessage(emailDocument);
        verify(emailMapper).mapToEmailDocument(emailData, emailData.getTo(), MessageType.APPLICATION_ACCEPTED);
    }

    @Test
    void sendApplicationOutcomeEmail_shouldGenerateAndSendAnApplicationRejectedEmail() {
        final Dissolution dissolution = generateDissolution();
        final DissolutionVerdict dissolutionVerdict = generateDissolutionVerdict();
        dissolutionVerdict.setResult(VerdictResult.REJECTED);
        dissolutionVerdict.setRejectReasons(Collections.singletonList(generateDissolutionRejectReason()));

        final ApplicationRejectedEmailData emailData = EmailFixtures.generateApplicationRejectedEmailData();
        final EmailDocument<ApplicationRejectedEmailData> emailDocument = generateEmailDocument(emailData);

        List<String> rejectReasonsAsStrings = dissolutionVerdict.getRejectReasons().stream().map(DissolutionRejectReason::getTextEnglish).collect(Collectors.toList());
        when(dissolutionEmailMapper.mapToApplicationRejectedEmailData(dissolution, rejectReasonsAsStrings, dissolution.getCreatedBy().getEmail())).thenReturn(emailData);
        when(messageTypeCalculator.getForApplicationRejected(dissolution)).thenReturn(MessageType.APPLICATION_REJECTED);
        when(emailMapper.mapToEmailDocument(emailData, emailData.getTo(), MessageType.APPLICATION_REJECTED)).thenReturn(emailDocument);

        dissolutionEmailService.sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);

        verify(emailMapper).mapToEmailDocument(emailData, emailData.getTo(), MessageType.APPLICATION_REJECTED);
        verify(emailService).sendMessage(emailDocument);
    }

    @Test
    void sendRejectionEmailToFinance_shouldGenerateAndSendAnApplicationRejectedEmail() {
        final String email = "email@finance.com";
        final Dissolution dissolution = generateDissolution();
        final DissolutionVerdict dissolutionVerdict = generateDissolutionVerdict();
        dissolutionVerdict.setResult(VerdictResult.REJECTED);
        dissolutionVerdict.setRejectReasons(Collections.singletonList(generateDissolutionRejectReason()));

        final ApplicationRejectedEmailData emailData = EmailFixtures.generateApplicationRejectedEmailData();
        final EmailDocument<ApplicationRejectedEmailData> emailDocument = generateEmailDocument(emailData);

        List<String> rejectReasonsAsStrings = dissolutionVerdict.getRejectReasons().stream().map(DissolutionRejectReason::getTextEnglish).collect(Collectors.toList());
        when(environmentConfig.getChsFinanceEmail()).thenReturn(email);
        when(dissolutionEmailMapper.mapToApplicationRejectedEmailData(dissolution, rejectReasonsAsStrings, email)).thenReturn(emailData);
        when(messageTypeCalculator.getForApplicationRejected(dissolution)).thenReturn(MessageType.APPLICATION_REJECTED);
        when(emailMapper.mapToEmailDocument(emailData, emailData.getTo(), MessageType.APPLICATION_REJECTED)).thenReturn(emailDocument);

        dissolutionEmailService.sendRejectionEmailToFinance(dissolution, dissolutionVerdict);

        verify(emailMapper).mapToEmailDocument(emailData, emailData.getTo(), MessageType.APPLICATION_REJECTED);
        verify(emailService).sendMessage(emailDocument);
    }

    @Test
    void notifySignatoriesToSign_shouldCalculateDeadline_andIdentifyUniqueSignatories() {
        final DissolutionDirector duplicateSignatoryOne = generateDissolutionDirector();
        duplicateSignatoryOne.setEmail(SIGNATORY_EMAIL_ONE);

        final DissolutionDirector duplicateSignatoryTwo = generateDissolutionDirector();
        duplicateSignatoryTwo.setEmail(SIGNATORY_EMAIL_ONE);

        final DissolutionDirector uniqueSignatory = generateDissolutionDirector();
        duplicateSignatoryTwo.setEmail(SIGNATORY_EMAIL_TWO);

        final Dissolution dissolution = generateDissolution();
        dissolution.getData().setDirectors(Arrays.asList(duplicateSignatoryOne, duplicateSignatoryTwo, uniqueSignatory));

        when(deadlineDateCalculator.calculateSignatoryDeadlineDate(any())).thenReturn(SIGNATORY_TO_SIGN_DEADLINE);

        dissolutionEmailService.notifySignatoriesToSign(dissolution);

        verify(deadlineDateCalculator).calculateSignatoryDeadlineDate(any());
        verify(dissolutionEmailMapper, times(1)).mapToSignatoryToSignEmailData(dissolution, SIGNATORY_EMAIL_ONE, SIGNATORY_TO_SIGN_DEADLINE);
        verify(dissolutionEmailMapper, times(1)).mapToSignatoryToSignEmailData(dissolution, SIGNATORY_EMAIL_TWO, SIGNATORY_TO_SIGN_DEADLINE);
    }

    @Test
    void notifySignatoriesToSign_shouldGenerateAndSendAnEmail_forEachUniqueSignatoryThatIsNotTheApplicant() {
        final DissolutionDirector signatoryOne = generateDissolutionDirector();
        signatoryOne.setEmail(SIGNATORY_EMAIL_ONE);

        final DissolutionDirector signatoryTwo = generateDissolutionDirector();
        signatoryTwo.setEmail(SIGNATORY_EMAIL_TWO);

        final DissolutionDirector applicant = generateDissolutionDirector();
        applicant.setEmail(APPLICANT_EMAIL);

        final Dissolution dissolution = generateDissolution();
        final CreatedBy createdBy = generateCreatedBy();
        createdBy.setEmail(APPLICANT_EMAIL);
        dissolution.setCreatedBy(createdBy);
        dissolution.getData().setDirectors(Arrays.asList(signatoryOne, signatoryTwo, applicant));

        final SignatoryToSignEmailData emailDataOne = generateSignatoryToSignEmailData();
        final SignatoryToSignEmailData emailDataTwo = generateSignatoryToSignEmailData();

        final EmailDocument<SignatoryToSignEmailData> emailOne = generateEmailDocument(emailDataOne);
        final EmailDocument<SignatoryToSignEmailData> emailTwo = generateEmailDocument(emailDataTwo);

        when(messageTypeCalculator.getForSignatoriesToSign(dissolution)).thenReturn(MessageType.SIGNATORY_TO_SIGN);
        when(deadlineDateCalculator.calculateSignatoryDeadlineDate(any())).thenReturn(SIGNATORY_TO_SIGN_DEADLINE);
        when(dissolutionEmailMapper.mapToSignatoryToSignEmailData(dissolution, SIGNATORY_EMAIL_ONE, SIGNATORY_TO_SIGN_DEADLINE)).thenReturn(emailDataOne);
        when(dissolutionEmailMapper.mapToSignatoryToSignEmailData(dissolution, SIGNATORY_EMAIL_TWO, SIGNATORY_TO_SIGN_DEADLINE)).thenReturn(emailDataTwo);
        when(emailMapper.mapToEmailDocument(emailDataOne, SIGNATORY_EMAIL_ONE, MessageType.SIGNATORY_TO_SIGN)).thenReturn(emailOne);
        when(emailMapper.mapToEmailDocument(emailDataTwo, SIGNATORY_EMAIL_TWO, MessageType.SIGNATORY_TO_SIGN)).thenReturn(emailTwo);

        dissolutionEmailService.notifySignatoriesToSign(dissolution);

        verify(emailService, times(2)).sendMessage(any());
        verify(emailService).sendMessage(emailOne);
        verify(emailService).sendMessage(emailTwo);
    }

    @Test
    void notifySignatoryToSign_shouldGenerateAndSendAnEmail_toSpecifiedEmail() {
        final DissolutionDirector signatoryOne = generateDissolutionDirector();
        signatoryOne.setEmail(SIGNATORY_EMAIL_ONE);

        final Dissolution dissolution = generateDissolution();
        dissolution.getData().setDirectors(Collections.singletonList(signatoryOne));

        final SignatoryToSignEmailData emailDataOne = generateSignatoryToSignEmailData();

        final EmailDocument<SignatoryToSignEmailData> emailOne = generateEmailDocument(emailDataOne);

        when(messageTypeCalculator.getForSignatoriesToSign(dissolution)).thenReturn(MessageType.SIGNATORY_TO_SIGN);
        when(deadlineDateCalculator.calculateSignatoryDeadlineDate(any())).thenReturn(SIGNATORY_TO_SIGN_DEADLINE);
        when(dissolutionEmailMapper.mapToSignatoryToSignEmailData(dissolution, SIGNATORY_EMAIL_ONE, SIGNATORY_TO_SIGN_DEADLINE)).thenReturn(emailDataOne);
        when(emailMapper.mapToEmailDocument(emailDataOne, SIGNATORY_EMAIL_ONE, MessageType.SIGNATORY_TO_SIGN)).thenReturn(emailOne);

        dissolutionEmailService.notifySignatoryToSign(dissolution, SIGNATORY_EMAIL_ONE);

        verify(emailService).sendMessage(emailOne);
    }

    @Test
    void sendFailedSubmissionNotificationEmail_shouldSendEmailToSupport() {
        final Dissolution dissolution = generateDissolution();
        final SupportNotificationEmailData emailData = generateSupportNotificationEmailData();
        final EmailDocument<SupportNotificationEmailData> emailDoc = generateEmailDocument(emailData);

        when(dissolutionEmailMapper.mapToSupportNotificationEmailData(dissolution)).thenReturn(emailData);
        when(emailMapper.mapToEmailDocument(emailData, emailData.getTo(), MessageType.SUBMISSION_TO_CHIPS_FAILED)).thenReturn(emailDoc);

        dissolutionEmailService.sendFailedSubmissionNotificationEmail(dissolution);

        verify(emailService).sendMessage(emailDoc);
    }
}
