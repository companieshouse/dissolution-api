package uk.gov.companieshouse.service.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.companieshouse.exception.InvalidTransactionStateException;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.fixtures.TransactionTestDataBuilder;
import uk.gov.companieshouse.mapper.FilingKindMapper;
import uk.gov.companieshouse.mapper.filing.FilingDataMapper;
import uk.gov.companieshouse.model.db.dissolution.DirectorApproval;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.domain.ValidationResult;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.FilingValidator;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.*;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionDirector;
import static uk.gov.companieshouse.fixtures.TransactionTestDataBuilder.aTransaction;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

@DisplayNameGeneration(ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class FilingServiceTest {

    private static final String DISSOLUTION_ID = "12345678";
    private static final String TRANSACTION_ID = "tx-id-123";
    private static final String PAYMENT_URI = String.format("/transactions/%s/payment", TRANSACTION_ID);
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String PAYMENT_METHOD = "credit-card";
    private static final String FILING_DESCRIPTION = "Dissolution application to strike off and dissolve a company %s (%s)";
    private static final String DISSOLUTION_FEE = "10.00";

    @Mock
    private DissolutionService dissolutionService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionPaymentService transactionPaymentService;

    @Mock
    private FeeConfig feeConfig;

    @Mock
    private FilingDataMapper filingDataMapper;

    @Mock
    private FilingValidator filingValidator;

    private FilingService filingService;

    private Transaction transaction;
    private Dissolution dissolution;
    private TransactionPayment transactionPayment;
    private PaymentApi paymentDetails;

    @BeforeEach
    void setup() {
        transaction = aTransaction()
                .withId(TRANSACTION_ID)
                .withStatus(CLOSED)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, DISSOLUTION_ID))
                .withPaymentLink(PAYMENT_URI)
                .build();

        dissolution = DissolutionTestDataBuilder.aDissolution().withId(DISSOLUTION_ID).build();

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

        final var filingKindMapper = new FilingKindMapper();
        filingService = new FilingService(dissolutionService, transactionService, transactionPaymentService, filingDataMapper, feeConfig, filingKindMapper, filingValidator);

        ReflectionTestUtils.setField(filingService, "filingDescription", FILING_DESCRIPTION);
    }

    @Test
    void when_dissolution_exists_then_filing_data_is_returned() {
        final var expectedDescription = String.format(FILING_DESCRIPTION, dissolution.getCompany().getName(), dissolution.getCompany().getNumber());
        final var expectedFilingData = TransactionFixtures.generateFilingData(dissolution);

        when(dissolutionService.getDissolutionById(DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI)).thenReturn(transactionPayment);
        when(transactionPaymentService.getPaymentSession(PAYMENT_REFERENCE)).thenReturn(paymentDetails);
        when(filingDataMapper.mapToFilingData(dissolution, PAYMENT_REFERENCE, PAYMENT_METHOD)).thenReturn(expectedFilingData);
        when(feeConfig.getClosingPounds()).thenReturn(DISSOLUTION_FEE);

        final var response = filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID);

        assertThat(response.getDescription()).isEqualTo(expectedDescription);
        assertThat(response.getKind()).isEqualTo(FILING_KIND_DS01);
        assertThat(response.getData()).isEqualTo(expectedFilingData);
        assertThat(response.getCost()).isEqualTo(DISSOLUTION_FEE);
    }

    @Test
    void when_application_type_is_llds01_then_filing_kind_is_llds01() {
        dissolution.getData().getApplication().setType(ApplicationType.LLDS01);

        when(dissolutionService.getDissolutionById(DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI)).thenReturn(transactionPayment);
        when(transactionPaymentService.getPaymentSession(PAYMENT_REFERENCE)).thenReturn(paymentDetails);

        final var response = filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID);

        assertThat(response.getKind()).isEqualTo(FILING_KIND_LLDS01);
    }

    @Test
    void when_dissolution_does_not_exist_then_dissolution_not_found_exception_thrown() {
        when(dissolutionService.getDissolutionById(DISSOLUTION_ID))
                .thenThrow(new DissolutionNotFoundException());

        assertThatThrownBy(() -> filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID))
                .isInstanceOf(DissolutionNotFoundException.class);
    }

    @Test
    void generateDissolutionFiling_throwsDissolutionNotLinkedToTransactionException_whenDissolutionNotLinkedToTransaction() {
        transaction = aTransaction()
                .withId(TRANSACTION_ID)
                .withStatus(CLOSED)
                .build();

        assertThatThrownBy(() -> filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID))
                .isInstanceOf(DissolutionNotLinkedToTransactionException.class);
    }

    @Test
    void generateDissolutionFiling_throwsInvalidTransactionStateException_whenTransactionIsNotClosed() {
        transaction = aTransaction()
                .withId(TRANSACTION_ID)
                .withStatus(OPEN)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, DISSOLUTION_ID))
                .withPaymentLink(PAYMENT_URI)
                .build();

        assertThatThrownBy(() -> filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID))
                .isInstanceOf(InvalidTransactionStateException.class);
    }

    @Test
    void generateDissolutionFiling_throwsServiceException_whenPaymentRetrievalFails() {
        when(dissolutionService.getDissolutionById(DISSOLUTION_ID)).thenReturn(dissolution);
        when(transactionService.getPayment(PAYMENT_URI))
                .thenThrow(new ServiceException("payment error", new RuntimeException()));

        assertThatThrownBy(() -> filingService.generateDissolutionFiling(transaction, DISSOLUTION_ID))
                .isInstanceOf(ServiceException.class);
    }

    @Nested
    class ValidateForFiling {

        @Test
        void when_dissolution_is_found_then_calls_validator_and_returns_its_result() {
            final var expectedResult = new ValidationResult();

            when(dissolutionService.getDissolutionById(DISSOLUTION_ID)).thenReturn(dissolution);
            when(filingValidator.validate(dissolution)).thenReturn(expectedResult);

            var result = filingService.validateForFiling(transaction, DISSOLUTION_ID);

            verify(filingValidator).validate(dissolution);
            assertThat(result).isSameAs(expectedResult);
        }

        @Test
        void when_dissolution_does_not_exist_then_exception_thrown() {
            when(dissolutionService.getDissolutionById(DISSOLUTION_ID))
                    .thenThrow(new DissolutionNotFoundException());

            assertThatThrownBy(() -> filingService.validateForFiling(transaction, DISSOLUTION_ID))
                    .isInstanceOf(DissolutionNotFoundException.class);
        }
    }
}
