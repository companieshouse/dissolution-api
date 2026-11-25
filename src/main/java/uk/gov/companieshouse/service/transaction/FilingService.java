package uk.gov.companieshouse.service.transaction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.mapper.FilingKindMapper;
import uk.gov.companieshouse.mapper.filing.FilingDataMapper;
import uk.gov.companieshouse.mapper.filing.FilingPaymentData;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.GenerateFilingCommand;
import uk.gov.companieshouse.model.domain.ValidateFilingCommand;
import uk.gov.companieshouse.model.domain.ValidationResult;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.FilingValidator;
import uk.gov.companieshouse.service.dissolution.validator.PaymentValidator;
import uk.gov.companieshouse.service.dissolution.validator.TransactionValidator;

@Service
public class FilingService {

    @Value("${dissolution.filingDescription}")
    private String filingDescription;

    private final DissolutionService dissolutionService;
    private final FilingPaymentDataResolver filingPaymentDataResolver;
    private final FilingDataMapper filingDataMapper;
    private final FilingKindMapper filingKindMapper;
    private final FeeConfig feeConfig;
    private final FilingValidator filingValidator;
    private final PaymentValidator paymentValidator;

    public FilingService(DissolutionService dissolutionService, FilingDataMapper filingDataMapper, FeeConfig feeConfig, FilingKindMapper filingKindMapper, FilingValidator filingValidator, FilingPaymentDataResolver filingPaymentDataResolver, PaymentValidator paymentValidator) {
        this.dissolutionService = dissolutionService;
        this.filingDataMapper = filingDataMapper;
        this.feeConfig = feeConfig;
        this.filingKindMapper = filingKindMapper;
        this.filingValidator = filingValidator;
        this.filingPaymentDataResolver = filingPaymentDataResolver;
        this.paymentValidator = paymentValidator;
    }

    public FilingApi generateDissolutionFiling(GenerateFilingCommand command) {
        var dissolution = dissolutionService.getDissolutionByTransactionAndCompany(command.transactionId(), command.companyNumber());

        TransactionValidator.of(command.transaction())
                .hasStatus(TransactionStatus.CLOSED)
                .forCompany(command.companyNumber())
                .isLinkedToDissolution(dissolution)
                .validate();

        final var payment = filingPaymentDataResolver.resolve(dissolution, command.transaction());

        paymentValidator.checkBusinessRules(payment).ifPresent(error -> {
            throw new ServiceException(String.format("Payment validation failed while generating dissolution filing data: %s", error));
        });

        return buildFiling(dissolution, payment);
    }

    /**
     * Determines whether a dissolution is currently in a state where it can be filed
     */
    public ValidationResult validateForFiling(ValidateFilingCommand command) {
        final var dissolution = dissolutionService.getDissolutionByTransactionAndCompany(command.transactionId(), command.companyNumber());

        TransactionValidator.of(command.transaction())
                .forCompany(command.companyNumber())
                .isLinkedToDissolution(dissolution)
                .validate();

        return filingValidator.validate(dissolution);
    }

    private FilingApi buildFiling(Dissolution dissolution, FilingPaymentData payment) {
        var filing = new FilingApi();
        filing.setKind(filingKindMapper.mapApplicationTypeToFilingKind(dissolution.getApplicationType()));
        // Note: the filing description is used to determine the email template and subject line
        filing.setDescription(filingDescription);
        // Cost is not set on account payments to prevent automatic refunds
        if (payment.method() == PaymentMethod.CREDIT_CARD) {
            filing.setCost(feeConfig.getClosingPounds());
        }
        filing.setData(filingDataMapper.mapToFilingData(dissolution, payment));
        return filing;
    }
}
