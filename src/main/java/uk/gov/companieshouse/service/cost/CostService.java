package uk.gov.companieshouse.service.cost;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.model.domain.DissolutionCost;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.TransactionValidator;

@Service
public class CostService {

    private final DissolutionService dissolutionService;
    private final FeeConfig feeConfig;

    public CostService(DissolutionService dissolutionService, FeeConfig feeConfig) {
        this.dissolutionService = dissolutionService;
        this.feeConfig = feeConfig;
    }

    public DissolutionCost getCosts(Transaction transaction, String dissolutionId) {
        TransactionValidator.of(transaction).isLinkedToDissolution(dissolutionId).validate();
        var dissolution = dissolutionService.getDissolutionById(dissolutionId);
        var company = dissolution.getCompany();
        var applicationType = dissolution.getApplicationType();

        return new DissolutionCost(feeConfig.getClosingPounds(), company.getName(), company.getNumber(), applicationType.getValue());
    }
}
