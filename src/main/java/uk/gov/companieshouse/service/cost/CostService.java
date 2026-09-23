package uk.gov.companieshouse.service.cost;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.model.domain.DissolutionCost;
import uk.gov.companieshouse.model.domain.GetDissolutionCostsCommand;
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

    public DissolutionCost getCosts(GetDissolutionCostsCommand command) {
        var dissolution = dissolutionService.getDissolutionByTransactionAndCompany(command.transactionId(), command.companyNumber());

        TransactionValidator.of(command.transaction())
                .forCompany(command.companyNumber())
                .isLinkedToDissolution(dissolution)
                .validate();

        var company = dissolution.getCompany();
        var applicationType = dissolution.getApplicationType();

        return new DissolutionCost(feeConfig.getClosingPounds(), company.getName(), company.getNumber(), applicationType.getValue());
    }
}
