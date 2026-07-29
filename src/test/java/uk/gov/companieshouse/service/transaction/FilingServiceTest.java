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
import uk.gov.companieshouse.api.model.transaction.TransactionLinks;
import uk.gov.companieshouse.api.model.transaction.TransactionPayment;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionDirector;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

@ExtendWith(MockitoExtension.class)
class FilingServiceTest {

    private static final String DISSOLUTION_ID = "12345678";
    private static final String TRANSACTION_ID = "def456";
    private static final String PASSTHROUGH_HEADER = "passthrough";
    private static final String PAYMENT_URI = String.format("/transactions/%s/payment", TRANSACTION_ID);
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String PAYMENT_METHOD = "credit-card";
    private static final String FILING_DESCRIPTION = "Apply to strike off and dissolve a company: %s (%s)";

    @Mock
    private DissolutionService dissolutionService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private Logger logger;

    @InjectMocks
    private FilingService filingService;

    private Transaction transaction;
    private Dissolution dissolution;
    private TransactionPayment transactionPayment;
    private PaymentApi paymentDetails;

    @BeforeEach
    void init() {
        transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setStatus(TransactionStatus.CLOSED);
        var transactionLinks = new TransactionLinks();
        transactionLinks.setPayment(PAYMENT_URI);
        transaction.setLinks(transactionLinks);

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
        var expectedFilingData = generateFilingData(dissolution, transactionPayment, paymentDetails);
        var expectedDescription = String.format(FILING_DESCRIPTION, dissolution.getCompany().getName(), dissolution.getCompany().getNumber());

        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER)).thenReturn(transactionPayment);
        when(paymentService.getPaymentSession(PAYMENT_REFERENCE, PASSTHROUGH_HEADER)).thenReturn(paymentDetails);

        var response = filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER);

        assertEquals(expectedDescription, response.getDescription());
        assertEquals(FILING_KIND_DS01, response.getKind());
//        assertEquals(response.getCost(), response);
        assertEquals(expectedFilingData, response.getData());

        verify(dissolutionService, times(1)).getDissolutionForTransaction(transaction, DISSOLUTION_ID);
        verify(transactionService, times(1)).getPayment(PAYMENT_URI, PASSTHROUGH_HEADER);
        verify(paymentService, times(1)).getPaymentSession(PAYMENT_REFERENCE, PASSTHROUGH_HEADER);
    }

    @Test
    void generateDissolutionFiling_setsKindToLLDS01_whenApplicationTypeIsLLDS01() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        dissolution.getData().getApplication().setType(ApplicationType.LLDS01);

        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER)).thenReturn(transactionPayment);
        when(paymentService.getPaymentSession(PAYMENT_REFERENCE, PASSTHROUGH_HEADER)).thenReturn(paymentDetails);

        var response = filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER);

        assertEquals(FILING_KIND_LLDS01, response.getKind());
    }

    @Test
    void generateDissolutionFiling_splitsDirectorName_intoSurnameAndForename_whenNameContainsComma() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        dissolution.getData().getDirectors().get(0).setName("DOE, John James");
        var expectedData = generateFilingData(dissolution, transactionPayment, paymentDetails);

        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER)).thenReturn(transactionPayment);
        when(paymentService.getPaymentSession(PAYMENT_REFERENCE, PASSTHROUGH_HEADER)).thenReturn(paymentDetails);

        var response = filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER);

        assertEquals(expectedData, response.getData());
    }

    @Test
    void generateDissolutionFiling_includesOnBehalfName_whenDirectorIsSigningOnBehalf() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        dissolution.getData().getDirectors().get(0).setOnBehalfName("Some Company Ltd");
        var expectedData = generateFilingData(dissolution, transactionPayment, paymentDetails);

        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER)).thenReturn(transactionPayment);
        when(paymentService.getPaymentSession(PAYMENT_REFERENCE, PASSTHROUGH_HEADER)).thenReturn(paymentDetails);

        var response = filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER);

        assertEquals(expectedData, response.getData());
    }

    @Test
    void generateDissolutionFiling_mapsAllDirectors_whenMultipleDirectors() throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        var approvalTwo = generateDirectorApproval();
        approvalTwo.setDateTime(LocalDateTime.of(2021, 5, 10, 0, 0));

        var directorTwo = generateDissolutionDirector();
        directorTwo.setName("SMITH, Jane");
        directorTwo.setEmail("jane@smith.com");
        directorTwo.setOnBehalfName(null);
        directorTwo.setDirectorApproval(approvalTwo);

        dissolution.getData().setDirectors(List.of(dissolution.getData().getDirectors().get(0), directorTwo));
        var expectedData = generateFilingData(dissolution, transactionPayment, paymentDetails);

        when(dissolutionService.getDissolutionForTransaction(transaction, DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER)).thenReturn(transactionPayment);
        when(paymentService.getPaymentSession(PAYMENT_REFERENCE, PASSTHROUGH_HEADER)).thenReturn(paymentDetails);

        var response = filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER);

        assertEquals(expectedData, response.getData());
        assertEquals(2, ((List<?>) response.getData().get("officers")).size());
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
        when(transactionService.getPayment(PAYMENT_URI, PASSTHROUGH_HEADER))
                .thenThrow(new ServiceException("payment error", new RuntimeException()));

        assertThrows(ServiceException.class,
                () -> filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID, PASSTHROUGH_HEADER));
    }

    private Map<String, Object> generateFilingData(Dissolution dissolution, TransactionPayment transactionPayment, PaymentApi paymentDetails) {
        Map<String, Object> data = new HashMap<>();
        data.put("company_name", dissolution.getCompany().getName());
        data.put("company_number", dissolution.getCompany().getNumber());
        data.put("officers", dissolution.getData().getDirectors().stream().map(this::mapToExpectedOfficer).toList());
        data.put("payment_reference", transactionPayment.getPaymentReference());
        data.put("payment_method", paymentDetails.getPaymentMethod());
        return data;
    }

    private Map<String, Object> mapToExpectedOfficer(DissolutionDirector director) {
        Map<String, Object> officer = new HashMap<>();
        officer.put("person_name", mapToExpectedPersonName(director.getName()));
        officer.put("sign_date", director.getDirectorApproval().getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        officer.put("email", director.getEmail());
        officer.put("ip_address", director.getDirectorApproval().getIpAddress());
        Optional.ofNullable(director.getOnBehalfName()).ifPresent(name -> officer.put("on_behalf_name", name));
        return officer;
    }

    private Map<String, String> mapToExpectedPersonName(String name) {
        Map<String, String> personName = new HashMap<>();
        int separatorIndex = name.indexOf(',');
        if (separatorIndex == -1) {
            personName.put("surname", name.trim());
        } else {
            personName.put("forename", name.substring(separatorIndex + 1).trim());
            personName.put("surname", name.substring(0, separatorIndex).trim());
        }
        return personName;
    }
}
