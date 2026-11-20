package uk.gov.companieshouse.service.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.payment.PaymentApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionPayment;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder;
import uk.gov.companieshouse.fixtures.PaymentFixtures;
import uk.gov.companieshouse.fixtures.TransactionFixtures;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.service.TransactionService;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.api.model.transaction.TransactionStatus.CLOSED;
import static uk.gov.companieshouse.fixtures.DirectorApprovalTestDataBuilder.aDirectorApproval;
import static uk.gov.companieshouse.fixtures.DissolutionDirectorTestDataBuilder.aDissolutionDirector;
import static uk.gov.companieshouse.fixtures.DissolutionTestDataBuilder.aDissolution;
import static uk.gov.companieshouse.fixtures.TransactionTestDataBuilder.aTransaction;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

@DisplayNameGeneration(ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class FilingPaymentDataResolverTest {

    private static final String DISSOLUTION_ID = "12345678";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String TRANSACTION_ID = "tx-id-123";
    private static final String PAYMENT_URI = String.format("/transactions/%s/payment", TRANSACTION_ID);
    private static final String PAYMENT_REFERENCE = "somePaymentRef";
    private static final String ACCOUNT_NUMBER = "222222";

    @Mock
    private TransactionService transactionService;

    @Mock
    private TransactionPaymentService transactionPaymentService;

    private FilingPaymentDataResolver resolver;

    private Transaction transaction;
    private Dissolution dissolution;
    private TransactionPayment transactionPayment;
    private PaymentApi paymentDetails;

    @BeforeEach
    void setup() {
        resolver = new FilingPaymentDataResolver(transactionService, transactionPaymentService);

        transaction = aTransaction()
                .withId(TRANSACTION_ID)
                .withStatus(CLOSED)
                .withResources(TransactionFixtures.generateTransactionResource(FILING_KIND_LLDS01, COMPANY_NUMBER))
                .withPaymentLink(PAYMENT_URI)
                .build();

        dissolution = aDissolutionApplication().build();

        transactionPayment = new TransactionPayment();
        transactionPayment.setPaymentReference(PAYMENT_REFERENCE);

        paymentDetails = new PaymentApi();
        paymentDetails.setPaymentMethod(PaymentFixtures.CREDIT_CARD_PAYMENT_METHOD);
    }

    @Test
    void when_dissolution_is_paid_by_account_then_account_payment_data_is_returned_and_no_payment_apis_are_called() {
        dissolution = aDissolutionApplication()
                .withPaymentInformation(PaymentFixtures.generateAccountPaymentInformation(ACCOUNT_NUMBER))
                .build();

        final var result = resolver.resolve(dissolution, transaction);

        assertThat(result).isEqualTo(PaymentFixtures.generateAccountPaymentData(ACCOUNT_NUMBER));
        verifyNoInteractions(transactionService, transactionPaymentService);
    }

    @Test
    void when_payment_information_is_missing_then_card_payment_data_is_resolved_from_transaction() {
        dissolution.setPaymentInformation(null);

        when(transactionService.getPayment(PAYMENT_URI)).thenReturn(transactionPayment);
        when(transactionPaymentService.getPaymentSession(PAYMENT_REFERENCE)).thenReturn(paymentDetails);

        final var result = resolver.resolve(dissolution, transaction);

        assertThat(result).isEqualTo(PaymentFixtures.generateCardPaymentData(PAYMENT_REFERENCE));
    }

    @Test
    void when_transaction_payment_retrieval_fails_then_service_exception_is_propagated() {
        when(transactionService.getPayment(PAYMENT_URI))
                .thenThrow(new ServiceException("payment error", new RuntimeException()));

        assertThatThrownBy(() -> resolver.resolve(dissolution, transaction))
                .isInstanceOf(ServiceException.class);

        verifyNoInteractions(transactionPaymentService);
    }

    @Test
    void when_payment_session_retrieval_fails_then_service_exception_is_propagated() {
        when(transactionService.getPayment(PAYMENT_URI)).thenReturn(transactionPayment);
        when(transactionPaymentService.getPaymentSession(PAYMENT_REFERENCE))
                .thenThrow(new ServiceException("payment session error", new RuntimeException()));

        assertThatThrownBy(() -> resolver.resolve(dissolution, transaction))
                .isInstanceOf(ServiceException.class);
    }

    private DissolutionTestDataBuilder aDissolutionApplication() {
        return aDissolution().withId(DISSOLUTION_ID)
                .withOnlyDirector(
                        aDissolutionDirector().withDirectorApproval(
                                aDirectorApproval().withDateTime(LocalDateTime.of(2020, Month.OCTOBER, 20, 0, 0))))
                .withTransactionId(TRANSACTION_ID);
    }
}
