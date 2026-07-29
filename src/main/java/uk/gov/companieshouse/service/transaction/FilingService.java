package uk.gov.companieshouse.service.transaction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.db.dissolution.Company;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.TransactionService;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.companieshouse.model.Constants.*;

@Service
public class FilingService {

    private record Context(Dissolution dissolution, Transaction transaction, String passThroughTokenHeader) {
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Value("${dissolution.filingDescription}")
    private String filingDescription;

    private final DissolutionService dissolutionService;
    private final TransactionService transactionService;
    private final PaymentService paymentService;
    private final Logger logger;

    public FilingService(DissolutionService dissolutionService, TransactionService transactionService, PaymentService paymentService, Logger logger) {
        this.dissolutionService = dissolutionService;
        this.transactionService = transactionService;
        this.paymentService = paymentService;
        this.logger = logger;
    }

    public FilingApi generateDissolutionFiling(Transaction transaction, String dissolutionId, String passThroughTokenHeader) throws DissolutionNotFoundException, ServiceException, DissolutionNotLinkedToTransactionException {
        logger.info(String.format("Generating dissolution filing for dissolution %s with transaction %s", dissolutionId, transaction.getId()));

        var filing = new FilingApi();
        var dissolution = dissolutionService.getDissolutionForTransaction(transaction, dissolutionId);
        var context = new Context(dissolution, transaction, passThroughTokenHeader);

        setFilingApiData(filing, context);
        return filing;
    }

    private void setFilingApiData(FilingApi filing, Context ctx) throws ServiceException {
        var dissolution = ctx.dissolution();
        var applicationType = dissolution.getData().getApplication().getType();
        final Company company = ctx.dissolution().getCompany();
        filing.setKind(applicationType == ApplicationType.LLDS01 ? FILING_KIND_LLDS01 : FILING_KIND_DS01);
        filing.setDescription(String.format(filingDescription, company.getName(), company.getNumber()));

        Map<String, Object> data = new HashMap<>();

        setCorporateBody(data, ctx);
        setOfficers(data, ctx);

//        data.put("sign_date", dissolution.getCreatedBy().getDateTime().format(DATE_FORMATTER));

        setPaymentData(data, ctx);
        filing.setData(data);
    }

    private void setCorporateBody(Map<String, Object> data, Context ctx) {
        final Company company = ctx.dissolution().getCompany();
        data.put("company_name", company.getName());
        data.put("company_number", company.getNumber());
    }

    private void setOfficers(Map<String, Object> data, Context ctx) {
        final List<DissolutionDirector> directors = ctx.dissolution().getData().getDirectors();
        data.put("officers", directors.stream().map(this::mapToOfficer).toList());
    }

    private void setPaymentData(Map<String, Object> data, Context ctx) {
        var transaction = ctx.transaction();
        var passThroughTokenHeader = ctx.passThroughTokenHeader();

        logger.info("Retrieving transaction payment details for: " + transaction.getId());

        var paymentDetails = transactionService.getPayment(transaction.getLinks().getPayment(), passThroughTokenHeader);
        var paymentReference = paymentDetails.getPaymentReference();

        logger.info("Retrieving payment data for dissolution filing with payment reference: " + paymentReference);

        var paymentSessionData = paymentService.getPaymentSession(paymentReference, passThroughTokenHeader);

        data.put("payment_reference", paymentReference);
        data.put("payment_method", paymentSessionData.getPaymentMethod());
    }

    private void setAttachment(Map<String, Object> data, Context ctx) {
        // TODO
    }

    private Map<String, Object> mapToOfficer(DissolutionDirector director) {
        final Map<String, Object> officer = new HashMap<>();

        officer.put("person_name", mapToPersonName(director.getName()));
        officer.put("sign_date", director.getDirectorApproval().getDateTime().format(formatter));
        officer.put("email", director.getEmail());
        officer.put("ip_address", director.getDirectorApproval().getIpAddress());

        Optional.ofNullable(director.getOnBehalfName()).ifPresent(name -> officer.put("on_behalf_name", name));

        return officer;
    }

    private Map<String, String> mapToPersonName(String name) {
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
