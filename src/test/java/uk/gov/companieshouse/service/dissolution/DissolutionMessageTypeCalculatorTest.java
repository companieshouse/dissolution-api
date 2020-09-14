package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.email.MessageType;
import uk.gov.companieshouse.model.enums.ApplicationType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolution;

public class DissolutionMessageTypeCalculatorTest {

    private final DissolutionMessageTypeCalculator calculator = new DissolutionMessageTypeCalculator();

    @Test
    public void getForSignatoriesToSign_returnsMessageType_forDs01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.DS01);

        final MessageType result = calculator.getForSignatoriesToSign(dissolution);

        assertEquals(MessageType.SIGNATORY_TO_SIGN, result);
    }

    @Test
    public void getForSignatoriesToSign_returnsMessageType_forLlds01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.LLDS01);

        final MessageType result = calculator.getForSignatoriesToSign(dissolution);

        assertEquals(MessageType.SIGNATORY_TO_SIGN_LLDS01, result);
    }

    @Test
    public void getForPendingPayment_returnsMessageType_forDs01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.DS01);

        final MessageType result = calculator.getForPendingPayment(dissolution);

        assertEquals(MessageType.PENDING_PAYMENT, result);
    }

    @Test
    public void getForPendingPayment_returnsMessageType_forLlds01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.LLDS01);

        final MessageType result = calculator.getForPendingPayment(dissolution);

        assertEquals(MessageType.PENDING_PAYMENT_LLDS01, result);
    }

    @Test
    public void getForSuccessfulPayment_returnsMessageType_forDs01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.DS01);

        final MessageType result = calculator.getForSuccessfulPayment(dissolution);

        assertEquals(MessageType.SUCCESSFUL_PAYMENT, result);
    }

    @Test
    public void getForSuccessfulPayment_returnsMessageType_forLlds01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.LLDS01);

        final MessageType result = calculator.getForSuccessfulPayment(dissolution);

        assertEquals(MessageType.SUCCESSFUL_PAYMENT_LLDS01, result);
    }

    @Test
    public void getForApplicationAccepted_returnsMessageType_forDs01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.DS01);

        final MessageType result = calculator.getForApplicationAccepted(dissolution);

        assertEquals(MessageType.APPLICATION_ACCEPTED, result);
    }

    @Test
    public void getForApplicationAccepted_returnsMessageType_forLlds01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.LLDS01);

        final MessageType result = calculator.getForApplicationAccepted(dissolution);

        assertEquals(MessageType.APPLICATION_ACCEPTED_LLDS01, result);
    }

    @Test
    public void getForApplicationRejected_returnsMessageType_forDs01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.DS01);

        final MessageType result = calculator.getForApplicationRejected(dissolution);

        assertEquals(MessageType.APPLICATION_REJECTED, result);
    }

    @Test
    public void getForApplicationRejected_returnsMessageType_forLlds01() {
        Dissolution dissolution = getDissolutionWithApplicationType(ApplicationType.LLDS01);

        final MessageType result = calculator.getForApplicationRejected(dissolution);

        assertEquals(MessageType.APPLICATION_REJECTED_LLDS01, result);
    }

    private Dissolution getDissolutionWithApplicationType(ApplicationType applicationType) {
        Dissolution dissolution = generateDissolution();

        dissolution.getData().getApplication().setType(applicationType);

        return dissolution;
    }
}
