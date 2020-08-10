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
import uk.gov.companieshouse.model.dto.email.EmailDocument;
import uk.gov.companieshouse.model.dto.email.SuccessfulPaymentEmailData;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DissolutionEmailServiceTest {

    @InjectMocks
    private DissolutionEmailService dissolutionEmailService;

    @Mock
    private DissolutionEmailMapper dissolutionEmailMapper;

    @Mock
    private EmailMapper<SuccessfulPaymentEmailData> emailMapper;

    @Mock
    private EmailService emailService;

    @Test
    public void sendSuccessfulPaymentEmail() {
        final Dissolution dissolution = DissolutionFixtures.generateDissolution();
        final SuccessfulPaymentEmailData successfulPaymentEmailData = EmailFixtures.generateSuccessfulPaymentEmailData();
        final EmailDocument<SuccessfulPaymentEmailData> emailDocument = EmailFixtures.generateEmailDocument(successfulPaymentEmailData);

        when(dissolutionEmailMapper.mapToSuccessfulPaymentEmailData(dissolution)).thenReturn(successfulPaymentEmailData);
        when(emailMapper.mapToEmailDocument(eq(successfulPaymentEmailData), eq(successfulPaymentEmailData.getTo()), any())).thenReturn(emailDocument);

        dissolutionEmailService.sendSuccessfulPaymentEmail(dissolution);

        verify(emailService).sendMessage(emailDocument);
    }
}
