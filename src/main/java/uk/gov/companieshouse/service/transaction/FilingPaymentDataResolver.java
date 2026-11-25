package uk.gov.companieshouse.service.transaction;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionLinks;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.mapper.filing.FilingPaymentData;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.service.TransactionService;

import java.util.Optional;

@Service
public class FilingPaymentDataResolver {

    private final TransactionService transactionService;
    private final TransactionPaymentService transactionPaymentService;

    public FilingPaymentDataResolver(TransactionService transactionService, TransactionPaymentService transactionPaymentService) {
        this.transactionService = transactionService;
        this.transactionPaymentService = transactionPaymentService;
    }

    public FilingPaymentData resolve(Dissolution dissolution, Transaction transaction) {
        if (isAccountPayment(dissolution)) {
            return FilingPaymentData.account(dissolution.getPaymentInformation().getAccountNumber());
        }

        final var paymentReference = getPaymentReference(transaction);
        final var paymentSession = transactionPaymentService.getPaymentSession(paymentReference);
        final var paymentMethod = paymentSession.getPaymentMethod();

        return parsePaymentMethod(paymentMethod)
                .filter(method -> method == PaymentMethod.CREDIT_CARD)
                .map(method -> FilingPaymentData.card(paymentReference))
                .orElseThrow(() -> new ServiceException(String.format("Transaction %s has unsupported payment method: %s", transaction.getId(), paymentMethod)));
    }

    private boolean isAccountPayment(Dissolution dissolution) {
        final PaymentInformation paymentInformation = dissolution.getPaymentInformation();
        return paymentInformation != null && paymentInformation.getMethod() == PaymentMethod.ACCOUNT;
    }

    private String getPaymentReference(Transaction transaction) {
        final var paymentUri = Optional.ofNullable(transaction.getLinks())
                .map(TransactionLinks::getPayment)
                .filter(uri -> !uri.isBlank())
                .orElseThrow(() -> new ServiceException(String.format("Transaction %s has no payment link", transaction.getId())));

        return transactionService.getPayment(paymentUri).getPaymentReference();
    }

    private Optional<PaymentMethod> parsePaymentMethod(String paymentMethod) {
        try {
            return Optional.of(PaymentMethod.fromValue(paymentMethod));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
