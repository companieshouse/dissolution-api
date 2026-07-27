package uk.gov.companieshouse.service.transaction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import static uk.gov.companieshouse.model.Constants.FILING_KIND_DS01;
import static uk.gov.companieshouse.model.Constants.FILING_KIND_LLDS01;

@Service
public class FilingService {

    @Value("${DISSOLUTION_FILING_DESCRIPTION}")
    private String filingDescription;

    private final DissolutionService dissolutionService;
    private final Logger logger;

    public FilingService(DissolutionService dissolutionService, Logger logger) {
        this.dissolutionService = dissolutionService;
        this.logger = logger;
    }

    public FilingApi generateDissolutionFiling(String companyNumber, Transaction transaction) throws DissolutionNotFoundException, ServiceException, DissolutionNotLinkedToTransactionException {
        var filing = new FilingApi();
        String transactionId = transaction.getId();

        logger.info(String.format("Generating dissolution filing for company %s with transaction %s", companyNumber, transactionId));

        var dissolution = dissolutionService.getByTransactionId(transaction.getId())
                .orElseThrow(() -> new DissolutionNotFoundException(
                        String.format("Empty submission returned when generating filing for %s", companyNumber)
                ));

        setFilingApiData(filing, dissolution, transaction);
        filing.setDescription(filingDescription);
        return filing;
    }

    private void setFilingApiData(FilingApi filing, Dissolution dissolution, Transaction transaction) throws DissolutionNotFoundException, ServiceException, DissolutionNotLinkedToTransactionException {
        var applicationType = dissolution.getData().getApplication().getType();
        filing.setKind(applicationType == ApplicationType.LLDS01 ? FILING_KIND_LLDS01 : FILING_KIND_DS01);
    }
}
