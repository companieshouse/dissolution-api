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
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.TransactionValidator;

import java.util.Map;

@Service
public class FilingService {

    private record Context(Dissolution dissolution, Transaction transaction) {
    }

    @Value("${dissolution.filingDescription}")
    private String filingDescription;

    private final DissolutionService dissolutionService;
    private final TransactionService transactionService;
    private final TransactionPaymentService transactionPaymentService;
    private final FilingDataMapper filingDataMapper;
    private final FilingKindMapper filingKindMapper;
    private final FeeConfig feeConfig;

    public FilingService(DissolutionService dissolutionService, TransactionService transactionService, TransactionPaymentService transactionPaymentService, FilingDataMapper filingDataMapper, FeeConfig feeConfig, FilingKindMapper filingKindMapper) {
        this.dissolutionService = dissolutionService;
        this.transactionService = transactionService;
        this.transactionPaymentService = transactionPaymentService;
        this.filingDataMapper = filingDataMapper;
        this.feeConfig = feeConfig;
        this.filingKindMapper = filingKindMapper;
    }

    public FilingApi generateDissolutionFiling(Transaction transaction, String dissolutionId) {
        TransactionValidator.of(transaction).hasStatus(TransactionStatus.CLOSED).isLinkedToDissolution(dissolutionId).validate();

        var filing = new FilingApi();
        var dissolution = dissolutionService.getDissolutionById(dissolutionId);
        var context = new Context(dissolution, transaction);

        setFilingApiData(filing, context);
        return filing;
    }

    private void setFilingApiData(FilingApi filing, Context ctx) throws ServiceException {
        final var dissolution = ctx.dissolution();
        final var company = ctx.dissolution().getCompany();
        final var kind = filingKindMapper.mapApplicationTypeToFilingKind(dissolution.getApplicationType());


        filing.setKind(kind);
        filing.setDescription(String.format(filingDescription, company.getName(), company.getNumber()));
        filing.setCost(feeConfig.getClosingPounds());
        filing.setData(buildFilingData(ctx));
    }

    private Map<String, Object> buildFilingData(Context ctx) {
        final var paymentDetails = transactionService.getPayment(
                ctx.transaction().getLinks().getPayment());
        final var paymentSession = transactionPaymentService.getPaymentSession(
                paymentDetails.getPaymentReference());

        return filingDataMapper.mapToFilingData(
                ctx.dissolution(),
                paymentDetails.getPaymentReference(),
                paymentSession.getPaymentMethod());
    }
}
