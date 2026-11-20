package uk.gov.companieshouse.service.transaction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.mapper.FilingKindMapper;
import uk.gov.companieshouse.mapper.filing.FilingDataMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.GenerateFilingCommand;
import uk.gov.companieshouse.model.domain.ValidateFilingCommand;
import uk.gov.companieshouse.model.domain.ValidationResult;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.FilingValidator;
import uk.gov.companieshouse.service.dissolution.validator.TransactionValidator;

@Service
public class FilingService {

    private record Context(Dissolution dissolution, Transaction transaction) {
    }

    @Value("${dissolution.filingDescription}")
    private String filingDescription;

    private final DissolutionService dissolutionService;
    private final FilingPaymentDataResolver filingPaymentDataResolver;
    private final FilingDataMapper filingDataMapper;
    private final FilingKindMapper filingKindMapper;
    private final FeeConfig feeConfig;
    private final FilingValidator filingValidator;

    public FilingService(DissolutionService dissolutionService, FilingDataMapper filingDataMapper, FeeConfig feeConfig, FilingKindMapper filingKindMapper, FilingValidator filingValidator, FilingPaymentDataResolver filingPaymentDataResolver) {
        this.dissolutionService = dissolutionService;
        this.filingDataMapper = filingDataMapper;
        this.feeConfig = feeConfig;
        this.filingKindMapper = filingKindMapper;
        this.filingValidator = filingValidator;
        this.filingPaymentDataResolver = filingPaymentDataResolver;
    }

    public FilingApi generateDissolutionFiling(GenerateFilingCommand command) {
        var dissolution = dissolutionService.getDissolutionByTransactionAndCompany(command.transactionId(), command.companyNumber());

        TransactionValidator.of(command.transaction())
                .hasStatus(TransactionStatus.CLOSED)
                .forCompany(command.companyNumber())
                .isLinkedToDissolution(dissolution)
                .validate();

        var filing = new FilingApi();
        var context = new Context(dissolution, command.transaction());

        setFilingApiData(filing, context);
        return filing;
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

        // TODO: validate account payment

        return filingValidator.validate(dissolution);
    }

    private void setFilingApiData(FilingApi filing, Context ctx) throws ServiceException {
        final var dissolution = ctx.dissolution();
        final var kind = filingKindMapper.mapApplicationTypeToFilingKind(dissolution.getApplicationType());
        final var payment = filingPaymentDataResolver.resolve(dissolution, ctx.transaction());
        final var data = filingDataMapper.mapToFilingData(dissolution, payment);

        filing.setKind(kind);
        // Note: the filing description is used to determine the email template and subject line
        filing.setDescription(filingDescription);
        filing.setCost(feeConfig.getClosingPounds()); // TODO: Set to 0 if payment is account
        filing.setData(data);
    }
}
