package uk.gov.companieshouse.service.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.InvalidTransactionStateException;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.mapper.FilingKindMapper;
import uk.gov.companieshouse.mapper.filing.FilingDataMapper;
import uk.gov.companieshouse.mapper.filing.FilingPaymentData;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.GenerateFilingCommand;
import uk.gov.companieshouse.model.domain.ValidateFilingCommand;
import uk.gov.companieshouse.model.domain.ValidationResult;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.FilingValidator;
import uk.gov.companieshouse.service.dissolution.validator.PaymentValidator;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.CLOSED;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.OPEN;
import static uk.gov.companieshouse.fixtures.DirectorApprovalTestDataBuilder.aDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.fixtures.TransactionTestDataBuilder.aTransaction;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

@DisplayNameGeneration(ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class FilingServiceTest {

    private static final String DISSOLUTION_ID = "12345678";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String TRANSACTION_ID = "tx-id-123";
    private static final String PAYMENT_URI = String.format("/transactions/%s/payment", TRANSACTION_ID);
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String ACCOUNT_NUMBER = "222222";
    private static final String FILING_DESCRIPTION = "Dissolution application to strike off and dissolve a company";
    private static final String DISSOLUTION_FEE = "10.00";
    private static final String ACCOUNT_PAYMENT_COST = "0";

    @Mock
    private DissolutionService dissolutionService;

    @Mock
    private FilingPaymentDataResolver filingPaymentDataResolver;

    @Mock
    private FeeConfig feeConfig;

    @Mock
    private FilingDataMapper filingDataMapper;

    @Mock
    private FilingValidator filingValidator;

    private FilingService filingService;

    private Transaction transaction;
    private Dissolution dissolution;
    private FilingPaymentData filingPaymentData;

    @BeforeEach
    void setup() {
        transaction = aTransaction()
                .withId(TRANSACTION_ID)
                .withStatus(CLOSED)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, COMPANY_NUMBER))
                .withPaymentLink(PAYMENT_URI)
                .build();

        dissolution = aDissolution().withId(DISSOLUTION_ID)
                .withOnlyDirector(
                        aDissolutionDirector().withDirectorApproval(
                                aDirectorApproval().withDateTime(LocalDateTime.of(2020, Month.OCTOBER, 20, 0, 0))))
                .withTransactionId(TRANSACTION_ID)
                .build();

        filingPaymentData = FilingPaymentData.card(PAYMENT_REFERENCE);

        final var filingKindMapper = new FilingKindMapper();
        filingService = new FilingService(dissolutionService, filingDataMapper, feeConfig, filingKindMapper, filingValidator, filingPaymentDataResolver, new PaymentValidator());

        ReflectionTestUtils.setField(filingService, "filingDescription", FILING_DESCRIPTION);
    }

    @Test
    void when_dissolution_exists_then_filing_data_is_returned() {
        final var expectedFilingData = TransactionFixtures.generateFilingData(dissolution);

        when(dissolutionService.getDissolutionByTransactionAndCompany(TRANSACTION_ID, COMPANY_NUMBER)).thenReturn(dissolution);
        when(filingPaymentDataResolver.resolve(dissolution, transaction)).thenReturn(filingPaymentData);
        when(filingDataMapper.mapToFilingData(dissolution, filingPaymentData)).thenReturn(expectedFilingData);
        when(feeConfig.getClosingPounds()).thenReturn(DISSOLUTION_FEE);

        final var response = filingService.generateDissolutionFiling(new GenerateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID));

        assertThat(response.getDescription()).isEqualTo(FILING_DESCRIPTION);
        assertThat(response.getKind()).isEqualTo(FILING_KIND_DS01);
        assertThat(response.getData()).isEqualTo(expectedFilingData);
        assertThat(response.getCost()).isEqualTo(DISSOLUTION_FEE);
    }

    @Test
    void when_application_type_is_llds01_then_filing_kind_is_llds01() {
        dissolution.getData().getApplication().setType(ApplicationType.LLDS01);

        when(dissolutionService.getDissolutionByTransactionAndCompany(TRANSACTION_ID, COMPANY_NUMBER)).thenReturn(dissolution);
        when(filingPaymentDataResolver.resolve(dissolution, transaction)).thenReturn(filingPaymentData);

        final var response = filingService.generateDissolutionFiling(new GenerateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID));

        assertThat(response.getKind()).isEqualTo(FILING_KIND_LLDS01);
    }

    @Test
    void when_payment_method_is_account_then_filing_cost_is_zero() {
        dissolution.getData().getApplication().setType(ApplicationType.LLDS01);

        when(dissolutionService.getDissolutionByTransactionAndCompany(TRANSACTION_ID, COMPANY_NUMBER)).thenReturn(dissolution);
        when(filingPaymentDataResolver.resolve(dissolution, transaction)).thenReturn(FilingPaymentData.account(ACCOUNT_NUMBER));

        final var response = filingService.generateDissolutionFiling(new GenerateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID));

        assertThat(response.getCost()).isNullOrEmpty();
    }

    @Test
    void when_dissolution_does_not_exist_then_dissolution_not_found_exception_thrown() {
        when(dissolutionService.getDissolutionByTransactionAndCompany(TRANSACTION_ID, COMPANY_NUMBER))
                .thenThrow(new DissolutionNotFoundException());

        final var command = new GenerateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID);

        assertThatThrownBy(() -> filingService.generateDissolutionFiling(command))
                .isInstanceOf(DissolutionNotFoundException.class);
    }

    @Test
    void generateDissolutionFiling_throwsDissolutionNotLinkedToTransactionException_whenDissolutionNotLinkedToTransaction() {
        transaction = aTransaction()
                .withId(TRANSACTION_ID)
                .withStatus(CLOSED)
                .build();

        when(dissolutionService.getDissolutionByTransactionAndCompany(TRANSACTION_ID, COMPANY_NUMBER)).thenReturn(dissolution);

        final var command = new GenerateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID);

        assertThatThrownBy(() -> filingService.generateDissolutionFiling(command))
                .isInstanceOf(DissolutionNotLinkedToTransactionException.class);
    }

    @Test
    void generateDissolutionFiling_throwsInvalidTransactionStateException_whenTransactionIsNotClosed() {
        transaction = aTransaction()
                .withId(TRANSACTION_ID)
                .withStatus(OPEN)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, COMPANY_NUMBER))
                .withPaymentLink(PAYMENT_URI)
                .build();

        final var command = new GenerateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID);

        assertThatThrownBy(() -> filingService.generateDissolutionFiling(command))
                .isInstanceOf(InvalidTransactionStateException.class);
    }

    @Test
    void generateDissolutionFiling_throwsServiceException_whenPaymentResolutionFails() {
        when(dissolutionService.getDissolutionByTransactionAndCompany(TRANSACTION_ID, COMPANY_NUMBER)).thenReturn(dissolution);
        when(filingPaymentDataResolver.resolve(dissolution, transaction))
                .thenThrow(new ServiceException("payment error", new RuntimeException()));

        final var command = new GenerateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID);

        assertThatThrownBy(() -> filingService.generateDissolutionFiling(command))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void when_payment_data_is_invalid_then_service_exception_thrown() {
        FilingPaymentData invalidPaymentData = new FilingPaymentData(PaymentMethod.ACCOUNT, PAYMENT_REFERENCE, ACCOUNT_NUMBER);

        when(dissolutionService.getDissolutionByTransactionAndCompany(TRANSACTION_ID, COMPANY_NUMBER)).thenReturn(dissolution);
        when(filingPaymentDataResolver.resolve(dissolution, transaction)).thenReturn(invalidPaymentData);

        final var command = new GenerateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID);

        assertThatThrownBy(() -> filingService.generateDissolutionFiling(command))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Payment validation failed while generating dissolution filing data: You cannot submit both a payment reference number and an account number");

        verifyNoInteractions(filingDataMapper);
    }

    @Nested
    class ValidateForFiling {

        @Test
        void when_dissolution_is_found_then_calls_validator_and_returns_its_result() {
            final var expectedResult = new ValidationResult();

            when(dissolutionService.getDissolutionByTransactionAndCompany(TRANSACTION_ID, COMPANY_NUMBER)).thenReturn(dissolution);
            when(filingValidator.validate(dissolution)).thenReturn(expectedResult);

            var result = filingService.validateForFiling(new ValidateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID));

            verify(filingValidator).validate(dissolution);
            assertThat(result).isSameAs(expectedResult);
        }

        @Test
        void when_dissolution_does_not_exist_then_exception_thrown() {
            when(dissolutionService.getDissolutionByTransactionAndCompany(TRANSACTION_ID, COMPANY_NUMBER))
                    .thenThrow(new DissolutionNotFoundException());

            final var command = new ValidateFilingCommand(transaction, COMPANY_NUMBER, TRANSACTION_ID);

            assertThatThrownBy(() -> filingService.validateForFiling(command))
                    .isInstanceOf(DissolutionNotFoundException.class);
        }
    }
}
