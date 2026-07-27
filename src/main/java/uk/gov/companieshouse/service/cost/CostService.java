package uk.gov.companieshouse.service.cost;

import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;

import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.ServiceException;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import static uk.gov.companieshouse.model.Constants.PAYMENT_AVAILABLE_PAYMENT_METHOD;
import static uk.gov.companieshouse.model.Constants.PAYMENT_CLASS_OF_PAYMENT;
import static uk.gov.companieshouse.model.Constants.PAYMENT_DESCRIPTION;
import static uk.gov.companieshouse.model.Constants.PAYMENT_DESCRIPTION_IDENTIFIER;
import static uk.gov.companieshouse.model.Constants.PAYMENT_ITEM_KIND;
import static uk.gov.companieshouse.model.Constants.PAYMENT_RESOURCE_KIND;

@Service
public class CostService {

    private final DissolutionService dissolutionService;
    private final FeeConfig feeConfig;

    public CostService(DissolutionService dissolutionService, FeeConfig feeConfig) {
        this.dissolutionService = dissolutionService;
        this.feeConfig = feeConfig;
    }

    public Cost getCosts(String dissolutionId) throws ServiceException, DissolutionNotFoundException {
        DissolutionGetResponse dissolutionInfo  = dissolutionService.getById(dissolutionId).orElseThrow(DissolutionNotFoundException::new);

        Cost cost = new Cost();
        cost.setAmount(feeConfig.getClosingPounds());
        cost.setClassOfPayment(List.of(PAYMENT_CLASS_OF_PAYMENT));
        cost.setAvailablePaymentMethods(List.of(PAYMENT_AVAILABLE_PAYMENT_METHOD));
        cost.setDescription(String.format(PAYMENT_DESCRIPTION, dissolutionInfo.getCompanyName(), dissolutionInfo.getCompanyNumber()));
        cost.setDescriptionIdentifier(PAYMENT_DESCRIPTION_IDENTIFIER);
        cost.setDescriptionValues(Collections.emptyMap());
        cost.setKind(PAYMENT_ITEM_KIND);
        cost.setResourceKind(PAYMENT_RESOURCE_KIND);
        cost.setProductType(dissolutionInfo.getApplicationType().getValue());

        return cost;
    }
}