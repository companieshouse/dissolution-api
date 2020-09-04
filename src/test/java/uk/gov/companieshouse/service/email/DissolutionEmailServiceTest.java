package uk.gov.companieshouse.service.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.EmailFixtures;
import uk.gov.companieshouse.mapper.email.DissolutionEmailMapper;
import uk.gov.companieshouse.mapper.email.EmailMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.db.dissolution.DissolutionRejectReason;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.dto.email.*;
import uk.gov.companieshouse.model.enums.VerdictResult;
import uk.gov.companieshouse.service.dissolution.DissolutionDeadlineDateCalculator;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionRejectReason;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionVerdict;
import static uk.gov.companieshouse.fixtures.EmailFixtures.generateEmailDocument;
import static uk.gov.companieshouse.fixtures.EmailFixtures.generateSignatoryToSignEmailData;
import static uk.gov.companieshouse.model.Constants.SIGNATORY_TO_SIGN_MESSAGE_TYPE;

@ExtendWith(MockitoExtension.class)
public class DissolutionEmailServiceTest {

    private static final String SIGNATORY_TO_SIGN_DEADLINE = "17 September 2020";
    private static final String SIGNATORY_EMAIL_ONE = "signatory1@mail.com";
    private static final String SIGNATORY_EMAIL_TWO = "signatory2@mail.com";

    @InjectMocks
    private DissolutionEmailService dissolutionEmailService;

    @Mock
    private DissolutionEmailMapper dissolutionEmailMapper;

    @Mock
    private EmailMapper emailMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private DissolutionDeadlineDateCalculator deadlineDateCalculator;

    @Test
    public void sendSuccessfulPaymentEmail_shouldGenerateAndSendASuccessfulPaymentEmail() {
        final Dissolution dissolution = generateDissolution();
        final SuccessfulPaymentEmailData successfulPaymentEmailData = EmailFixtures.generateSuccessfulPaymentEmailData();
        final EmailDocument<SuccessfulPaymentEmailData> emailDocument = generateEmailDocument(successfulPaymentEmailData);

        when(dissolutionEmailMapper.mapToSuccessfulPaymentEmailData(dissolution)).thenReturn(successfulPaymentEmailData);
        when(emailMapper.mapToEmailDocument(eq(successfulPaymentEmailData), eq(successfulPaymentEmailData.getTo()), any())).thenReturn(emailDocument);

        dissolutionEmailService.sendSuccessfulPaymentEmail(dissolution);

        verify(emailService).sendMessage(emailDocument);
    }

    @Test
    public void sendPendingPaymentEmail_shouldGenerateAndSendAPendingPaymentEmail() {
        final Dissolution dissolution = generateDissolution();
        final PendingPaymentEmailData pendingPaymentEmailData = EmailFixtures.generatePendingPaymentEmailData();
        final EmailDocument<PendingPaymentEmailData> emailDocument = generateEmailDocument(pendingPaymentEmailData);

        when(dissolutionEmailMapper.mapToPendingPaymentEmailData(dissolution)).thenReturn(pendingPaymentEmailData);
        when(emailMapper.mapToEmailDocument(eq(pendingPaymentEmailData), eq(pendingPaymentEmailData.getTo()), any())).thenReturn(emailDocument);

        dissolutionEmailService.sendPendingPaymentEmail(dissolution);

        verify(emailService).sendMessage(emailDocument);
    }

    @Test
    public void sendApplicationOutcomeEmail_shouldGenerateAndSendAnApplicationAcceptedEmail() {
        final Dissolution dissolution = generateDissolution();
        final DissolutionVerdict dissolutionVerdict = generateDissolutionVerdict();

        final ApplicationAcceptedEmailData applicationAcceptedEmailData = EmailFixtures.generateApplicationAcceptedEmailData();
        final EmailDocument<ApplicationAcceptedEmailData> emailDocument = generateEmailDocument(applicationAcceptedEmailData);

        when(dissolutionEmailMapper.mapToApplicationAcceptedEmailData(dissolution)).thenReturn(applicationAcceptedEmailData);
        when(emailMapper.mapToEmailDocument(eq(applicationAcceptedEmailData), eq(applicationAcceptedEmailData.getTo()), any())).thenReturn(emailDocument);

        dissolutionEmailService.sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);

        verify(emailService).sendMessage(emailDocument);
    }

    @Test
    public void sendApplicationOutcomeEmail_shouldGenerateAndSendAnApplicationRejectedEmail() {
        final Dissolution dissolution = generateDissolution();
        final DissolutionVerdict dissolutionVerdict = generateDissolutionVerdict();
        dissolutionVerdict.setResult(VerdictResult.REJECTED);
        dissolutionVerdict.setRejectReasons(Collections.singletonList(generateDissolutionRejectReason()));

        final ApplicationRejectedEmailData applicationRejectedEmailData = EmailFixtures.generateApplicationRejectedEmailData();
        final EmailDocument<ApplicationRejectedEmailData> emailDocument = generateEmailDocument(applicationRejectedEmailData);

        List<String> rejectReasonsAsStrings = dissolutionVerdict.getRejectReasons().stream().map(DissolutionRejectReason::getTextEnglish).collect(Collectors.toList());
        when(dissolutionEmailMapper.mapToApplicationRejectedEmailData(dissolution, rejectReasonsAsStrings)).thenReturn(applicationRejectedEmailData);
        when(emailMapper.mapToEmailDocument(eq(applicationRejectedEmailData), eq(applicationRejectedEmailData.getTo()), any())).thenReturn(emailDocument);

        dissolutionEmailService.sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);

        verify(emailService).sendMessage(emailDocument);
    }

    @Test
    public void notifySignatoriesToSign_shouldCalculateDeadline_andIdentifyUniqueSignatories() {
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
    public void notifySignatoriesToSign_shouldGenerateAndSendAnEmail_forEachUniqueSignatory() {
        final DissolutionDirector signatoryOne = generateDissolutionDirector();
        signatoryOne.setEmail(SIGNATORY_EMAIL_ONE);

        final DissolutionDirector signatoryTwo = generateDissolutionDirector();
        signatoryTwo.setEmail(SIGNATORY_EMAIL_TWO);

        final Dissolution dissolution = generateDissolution();
        dissolution.getData().setDirectors(Arrays.asList(signatoryOne, signatoryTwo));

        final SignatoryToSignEmailData emailDataOne = generateSignatoryToSignEmailData();
        final SignatoryToSignEmailData emailDataTwo = generateSignatoryToSignEmailData();

        final EmailDocument<SignatoryToSignEmailData> emailOne = generateEmailDocument(emailDataOne);
        final EmailDocument<SignatoryToSignEmailData> emailTwo = generateEmailDocument(emailDataTwo);

        when(deadlineDateCalculator.calculateSignatoryDeadlineDate(any())).thenReturn(SIGNATORY_TO_SIGN_DEADLINE);
        when(dissolutionEmailMapper.mapToSignatoryToSignEmailData(dissolution, SIGNATORY_EMAIL_ONE, SIGNATORY_TO_SIGN_DEADLINE)).thenReturn(emailDataOne);
        when(dissolutionEmailMapper.mapToSignatoryToSignEmailData(dissolution, SIGNATORY_EMAIL_TWO, SIGNATORY_TO_SIGN_DEADLINE)).thenReturn(emailDataTwo);
        when(emailMapper.mapToEmailDocument(emailDataOne, SIGNATORY_EMAIL_ONE, SIGNATORY_TO_SIGN_MESSAGE_TYPE)).thenReturn(emailOne);
        when(emailMapper.mapToEmailDocument(emailDataTwo, SIGNATORY_EMAIL_TWO, SIGNATORY_TO_SIGN_MESSAGE_TYPE)).thenReturn(emailTwo);

        dissolutionEmailService.notifySignatoriesToSign(dissolution);

        verify(emailService, times(2)).sendMessage(any());
        verify(emailService).sendMessage(emailOne);
        verify(emailService).sendMessage(emailTwo);
    }
}
