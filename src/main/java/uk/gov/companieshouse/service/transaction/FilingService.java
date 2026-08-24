package uk.gov.companieshouse.service.transaction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.mapper.filing.FilingDataMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.TransactionValidator;

import java.util.Map;

import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

@Service
public class FilingService {

    private record Context(Dissolution dissolution, Transaction transaction) {
    }

    @Value("${dissolution.filingDescription}")
    private String filingDescription;

    private final DissolutionService dissolutionService;
    private final TransactionService transactionService;
    private final TransactionPaymentService transactionPaymentService;
    private final FilingDataMapper mapper;
    private final FeeConfig feeConfig;

    public FilingService(DissolutionService dissolutionService, TransactionService transactionService, TransactionPaymentService transactionPaymentService, FilingDataMapper mapper, FeeConfig feeConfig) {
        this.dissolutionService = dissolutionService;
        this.transactionService = transactionService;
        this.transactionPaymentService = transactionPaymentService;
        this.mapper = mapper;
        this.feeConfig = feeConfig;
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
        final var applicationType = dissolution.getData().getApplication().getType();

        filing.setKind(applicationType == ApplicationType.LLDS01 ? FILING_KIND_LLDS01 : FILING_KIND_DS01);
        filing.setDescription(String.format(filingDescription, company.getName(), company.getNumber()));
        filing.setCost(feeConfig.getClosingPounds());
        filing.setData(buildFilingData(ctx));
    }

    private Map<String, Object> buildFilingData(Context ctx) {
        final var paymentDetails = transactionService.getPayment(
                ctx.transaction().getLinks().getPayment());
        final var paymentSession = transactionPaymentService.getPaymentSession(
                paymentDetails.getPaymentReference());

        return mapper.mapToFilingData(
                ctx.dissolution(),
                paymentDetails.getPaymentReference(),
                paymentSession.getPaymentMethod());
    }
}
