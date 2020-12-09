package uk.gov.companieshouse.service.dissolution.chips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.fixtures.ChipsFixtures;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.PaymentFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.DissolutionVerdictMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionVerdict;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.model.enums.VerdictResult;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;
import uk.gov.companieshouse.service.dissolution.DissolutionRefundService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChipsResponseServiceTest {

    @InjectMocks
    private ChipsResponseService chipsResponseService;

    @Mock
    private DissolutionRepository repository;

    @Mock
    private DissolutionVerdictMapper dissolutionVerdictMapper;

    @Mock
    private DissolutionEmailService dissolutionEmailService;

    @Mock
    private DissolutionRefundService dissolutionRefundService;

    @Mock
    private Logger logger;

    @Test
    void saveAndNotifyDissolutionApplicationOutcome_saveDissolutionApplicationOutcomeAndSendEmail_acceptedApplication() throws DissolutionNotFoundException {
        ChipsResponseCreateRequest chipsResponseCreateRequest = ChipsFixtures.generateChipsResponseCreateRequest();
        chipsResponseCreateRequest.setStatus(VerdictResult.ACCEPTED);
        DissolutionVerdict dissolutionVerdict = DissolutionFixtures.generateDissolutionVerdict();
        dissolutionVerdict.setResult(VerdictResult.ACCEPTED);
        Dissolution dissolution = DissolutionFixtures.generateDissolution();

        when(dissolutionVerdictMapper.mapToDissolutionVerdict(chipsResponseCreateRequest)).thenReturn(dissolutionVerdict);
        when(repository.findByDataApplicationReference(chipsResponseCreateRequest.getSubmissionReference())).thenReturn(Optional.of(dissolution));

        chipsResponseService.saveAndNotifyDissolutionApplicationOutcome(chipsResponseCreateRequest);
        
        assertFalse(dissolution.getActive());

        verifyNoInteractions(dissolutionRefundService);
        verify(dissolutionEmailService).sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);
    }

    @Test
    void saveAndNotifyDissolutionApplicationOutcome_saveDissolutionApplicationOutcomeAndSendEmail_rejectedApplicationAndCardPayment() throws DissolutionNotFoundException {
        ChipsResponseCreateRequest chipsResponseCreateRequest = ChipsFixtures.generateChipsResponseCreateRequest();
        chipsResponseCreateRequest.setStatus(VerdictResult.REJECTED);
        DissolutionVerdict dissolutionVerdict = DissolutionFixtures.generateDissolutionVerdict();
        dissolutionVerdict.setResult(VerdictResult.REJECTED);
        Dissolution dissolution = DissolutionFixtures.generateDissolution();
        PaymentInformation paymentInformation = PaymentFixtures.generatePaymentInformation();
        dissolution.setPaymentInformation(paymentInformation);

        when(dissolutionVerdictMapper.mapToDissolutionVerdict(chipsResponseCreateRequest)).thenReturn(dissolutionVerdict);
        when(repository.findByDataApplicationReference(chipsResponseCreateRequest.getSubmissionReference())).thenReturn(Optional.of(dissolution));

        chipsResponseService.saveAndNotifyDissolutionApplicationOutcome(chipsResponseCreateRequest);

        assertFalse(dissolution.getActive());

        verify(dissolutionRefundService).handleRefund(dissolution, dissolutionVerdict);
        verify(dissolutionEmailService).sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);
    }

    @Test
    void saveAndNotifyDissolutionApplicationOutcome_saveDissolutionApplicationOutcomeAndSendEmail_rejectedApplicationAndAccountPayment() throws DissolutionNotFoundException {
        ChipsResponseCreateRequest chipsResponseCreateRequest = ChipsFixtures.generateChipsResponseCreateRequest();
        chipsResponseCreateRequest.setStatus(VerdictResult.REJECTED);
        DissolutionVerdict dissolutionVerdict = DissolutionFixtures.generateDissolutionVerdict();
        dissolutionVerdict.setResult(VerdictResult.REJECTED);
        Dissolution dissolution = DissolutionFixtures.generateDissolution();
        PaymentInformation paymentInformation = PaymentFixtures.generatePaymentInformation();
        paymentInformation.setMethod(PaymentMethod.ACCOUNT);
        dissolution.setPaymentInformation(paymentInformation);

        when(dissolutionVerdictMapper.mapToDissolutionVerdict(chipsResponseCreateRequest)).thenReturn(dissolutionVerdict);
        when(repository.findByDataApplicationReference(chipsResponseCreateRequest.getSubmissionReference())).thenReturn(Optional.of(dissolution));

        chipsResponseService.saveAndNotifyDissolutionApplicationOutcome(chipsResponseCreateRequest);

        assertFalse(dissolution.getActive());

        verify(dissolutionRefundService, never()).handleRefund(dissolution, dissolutionVerdict);
        verify(dissolutionEmailService).sendApplicationOutcomeEmail(dissolution, dissolutionVerdict);
    }
}
