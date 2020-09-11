package uk.gov.companieshouse.service.dissolution;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.email.MessageType;
import uk.gov.companieshouse.model.enums.ApplicationType;

@Service
public class DissolutionMessageTypeCalculator {

    public MessageType getForSignatoriesToSign(Dissolution dissolution) {
        return getMessageTypeForDissolution(dissolution, MessageType.SIGNATORY_TO_SIGN_LLDS01, MessageType.SIGNATORY_TO_SIGN);
    }

    public MessageType getForPendingPayment(Dissolution dissolution) {
        return getMessageTypeForDissolution(dissolution, MessageType.PENDING_PAYMENT_LLDS01, MessageType.PENDING_PAYMENT);
    }

    public MessageType getForSuccessfulPayment(Dissolution dissolution) {
        return getMessageTypeForDissolution(dissolution, MessageType.SUCCESSFUL_PAYMENT_LLDS01, MessageType.SUCCESSFUL_PAYMENT);
    }

    public MessageType getForApplicationAccepted(Dissolution dissolution) {
        return getMessageTypeForDissolution(dissolution, MessageType.APPLICATION_ACCEPTED_LLDS01, MessageType.APPLICATION_ACCEPTED);
    }

    public MessageType getForApplicationRejected(Dissolution dissolution) {
        return getMessageTypeForDissolution(dissolution, MessageType.APPLICATION_REJECTED_LLDS01, MessageType.APPLICATION_REJECTED);
    }

    private MessageType getMessageTypeForDissolution(Dissolution dissolution, MessageType llds01Type, MessageType ds01Type) {
        return dissolution.getData().getApplication().getType().equals(ApplicationType.LLDS01) ? llds01Type : ds01Type;
    }
}
