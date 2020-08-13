package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.email.DissolutionEmailMapper;
import uk.gov.companieshouse.mapper.email.EmailMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;
import uk.gov.companieshouse.service.email.EmailService;

import static uk.gov.companieshouse.model.Constants.SUCCESSFUL_PAYMENT_MESSAGE_TYPE;

@Service
public class DissolutionEmailService {

    private final DissolutionEmailMapper dissolutionEmailMapper;
    private final EmailMapper<SuccessfulPaymentEmailData> emailMapper;
    private final EmailService emailService;

    @Autowired
    public DissolutionEmailService(
            DissolutionEmailMapper dissolutionEmailMapper, EmailMapper<SuccessfulPaymentEmailData> emailMapper,
            EmailService emailService
    ) {
        this.dissolutionEmailMapper = dissolutionEmailMapper;
        this.emailMapper = emailMapper;
        this.emailService = emailService;
    }

    public void sendSuccessfulPaymentEmail(Dissolution dissolution) {
        final SuccessfulPaymentEmailData successfulPaymentEmailData =
                this.dissolutionEmailMapper.mapToSuccessfulPaymentEmailData(dissolution);

        final EmailDocument<SuccessfulPaymentEmailData> emailDocument = this.emailMapper.mapToEmailDocument(
                successfulPaymentEmailData, successfulPaymentEmailData.getTo(), SUCCESSFUL_PAYMENT_MESSAGE_TYPE
        );

        emailService.sendMessage(emailDocument);
    }
}
