package uk.gov.companieshouse.service.transaction;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.mapper.filing.FilingPaymentData;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.service.TransactionService;

@Service
public class FilingPaymentDataResolver {

    private final TransactionService transactionService;
    private final TransactionPaymentService transactionPaymentService;

    public FilingPaymentDataResolver(TransactionService transactionService, TransactionPaymentService transactionPaymentService) {
        this.transactionService = transactionService;
        this.transactionPaymentService = transactionPaymentService;
    }

    public FilingPaymentData resolve(Dissolution dissolution, Transaction transaction) {
        final PaymentInformation paymentInformation = dissolution.getPaymentInformation();

        if (isAccountPayment(dissolution)) {
            return new FilingPaymentData(PaymentMethod.ACCOUNT.getValue(), null, paymentInformation.getAccountNumber());
        }

        final var paymentDetails = transactionService.getPayment(transaction.getLinks().getPayment());
        final var paymentSession = transactionPaymentService.getPaymentSession(paymentDetails.getPaymentReference());

        return new FilingPaymentData(paymentSession.getPaymentMethod(), paymentDetails.getPaymentReference(), null);
    }

    public boolean isAccountPayment(Dissolution dissolution) {
        final PaymentInformation paymentInformation = dissolution.getPaymentInformation();
        return paymentInformation != null && paymentInformation.getMethod() == PaymentMethod.ACCOUNT;
    }
}
