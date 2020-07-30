package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DissolutionEmailMapper;
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;
import uk.gov.companieshouse.service.email.EmailService;

@Service
public class DissolutionEmailService {

    private final DissolutionEmailMapper dissolutionEmailMapper;
    private final EmailService emailService;

    @Autowired
    public DissolutionEmailService(DissolutionEmailMapper dissolutionEmailMapper, EmailService emailService) {
        this.dissolutionEmailMapper = dissolutionEmailMapper;
        this.emailService = emailService;
    }

    public void sendSuccessfulPaymentEmail(
        String dissolutionReferenceNumber, String companyNumber, String companyName, String emailAddress
    ) {
        final EmailDocument<SuccessfulPaymentEmailData> emailDocument = this.dissolutionEmailMapper.mapToEmailDocument(
            dissolutionReferenceNumber, companyNumber, companyName, emailAddress
        );

        emailService.sendMessage(emailDocument);
    }
}
