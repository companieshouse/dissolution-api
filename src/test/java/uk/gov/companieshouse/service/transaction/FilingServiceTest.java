package uk.gov.companieshouse.service.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionPayment;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.filing.FilingDataMapper;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionDirector;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

@ExtendWith(MockitoExtension.class)
class FilingServiceTest {

    private static final String DISSOLUTION_ID = "12345678";
    private static final String TRANSACTION_ID = "tx-id-123";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String PAYMENT_URI = String.format("/transactions/%s/payment", TRANSACTION_ID);
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String PAYMENT_METHOD = "credit-card";
    private static final String FILING_DESCRIPTION = "Dissolution application to strike off and dissolve a company %s (%s)";

    @Mock
    private DissolutionService dissolutionService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionPaymentService transactionPaymentService;

    @Mock
    private FeeConfig feeConfig;

    @Mock
    private Logger logger;

    @Mock
    private FilingDataMapper mapper;

    @InjectMocks
    private FilingService filingService;

    private Transaction transaction;
    private Dissolution dissolution;
    private TransactionPayment transactionPayment;
    private PaymentApi paymentDetails;

    @BeforeEach
    void setup() {
        transaction = TransactionTestDataBuilder.aTransaction()
                .withId(TRANSACTION_ID)
                .withStatus(TransactionStatus.CLOSED)
                .withPaymentLink(PAYMENT_URI)
                .build();

        dissolution = DissolutionFixtures.generateDissolution();
        dissolution.setId(DISSOLUTION_ID);

        final DirectorApproval approvalOne = generateDirectorApproval();
        approvalOne.setDateTime(LocalDateTime.of(2020, 10, 20, 0, 0));

        final DissolutionDirector directorOne = generateDissolutionDirector();
        directorOne.setName("Director One");
        directorOne.setOnBehalfName(null);
        directorOne.setDirectorApproval(approvalOne);

        dissolution.getData().setDirectors(List.of(directorOne));

        transactionPayment = new TransactionPayment();
        transactionPayment.setPaymentReference(PAYMENT_REFERENCE);

        paymentDetails = new PaymentApi();
        paymentDetails.setPaymentMethod(PAYMENT_METHOD);

        ReflectionTestUtils.setField(filingService, "filingDescription", FILING_DESCRIPTION);
    }

    @Test
    void generateDissolutionFiling_returnsFilingData() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        var expectedDescription = String.format(FILING_DESCRIPTION, dissolution.getCompany().getName(), dissolution.getCompany().getNumber());
        var expectedFilingData = TransactionFixtures.generateFilingData(dissolution);

        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI)).thenReturn(transactionPayment);
        when(transactionPaymentService.getPaymentSession(PAYMENT_REFERENCE, PASSTHROUGH_HEADER)).thenReturn(paymentDetails);
        when(mapper.mapToFilingData(dissolution, PAYMENT_REFERENCE, PAYMENT_METHOD)).thenReturn(expectedFilingData);
        when(feeConfig.getClosingPounds()).thenReturn("10.00");

        var response = filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER);

        assertEquals(expectedDescription, response.getDescription());
        assertEquals(FILING_KIND_DS01, response.getKind());
        assertEquals(expectedFilingData, response.getData());
        assertEquals("10.00", response.getCost());

        verify(dissolutionService, times(1)).getDissolutionForTransaction(transaction, DISSOLUTION_ID);
        verify(transactionService, times(1)).getPayment(PAYMENT_URI);
        verify(transactionPaymentService, times(1)).getPaymentSession(PAYMENT_REFERENCE, PASSTHROUGH_HEADER);
        verify(mapper, times(1)).mapToFilingData(dissolution, PAYMENT_REFERENCE, PAYMENT_METHOD);
        verify(feeConfig, times(1)).getClosingPounds();
    }

    @Test
    void generateDissolutionFiling_setsKindToLLDS01_whenApplicationTypeIsLLDS01() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        dissolution.getData().getApplication().setType(ApplicationType.LLDS01);

        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI)).thenReturn(transactionPayment);
        when(transactionPaymentService.getPaymentSession(PAYMENT_REFERENCE, PASSTHROUGH_HEADER)).thenReturn(paymentDetails);

        var response = filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER);

        assertEquals(FILING_KIND_LLDS01, response.getKind());
    }

    @Test
    void generateDissolutionFiling_throwsDissolutionNotFoundException_whenDissolutionDoesNotExist() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID))
                .thenThrow(new DissolutionNotFoundException());

        assertThrows(DissolutionNotFoundException.class,
                () -> filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER));
    }

    @Test
    void generateDissolutionFiling_throwsDissolutionNotLinkedToTransactionException_whenDissolutionNotLinkedToTransaction() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID))
                .thenThrow(new DissolutionNotLinkedToTransactionException("not linked"));

        assertThrows(DissolutionNotLinkedToTransactionException.class,
                () -> filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER));
    }

    @Test
    void generateDissolutionFiling_throwsServiceException_whenPaymentRetrievalFails() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI))
                .thenThrow(new ServiceException("payment error", new RuntimeException()));

        assertThrows(ServiceException.class,
                () -> filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER));
    }
}
