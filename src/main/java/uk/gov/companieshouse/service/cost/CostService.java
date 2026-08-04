package uk.gov.companieshouse.service.cost;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import java.util.Collections;
import java.util.List;

import static uk.gov.companieshouse.model.Constants.PAYMENT_AVAILABLE_PAYMENT_METHOD;
import static uk.gov.companieshouse.model.Constants.PAYMENT_CLASS_OF_PAYMENT;
import static uk.gov.companieshouse.model.Constants.PAYMENT_DESCRIPTION;
import static uk.gov.companieshouse.model.Constants.PAYMENT_DESCRIPTION_IDENTIFIER;

@Service
public class CostService {

    private final DissolutionService dissolutionService;
    private final FeeConfig feeConfig;
    private static final String PAYMENT_SESSION = "payment-session#payment-session";
    private static final String RESOURCE_KIND = "dissolution";

    public CostService(DissolutionService dissolutionService, FeeConfig feeConfig) {
        this.dissolutionService = dissolutionService;
        this.feeConfig = feeConfig;
    }

    public Cost getCosts(Transaction transaction, String dissolutionId) throws DissolutionNotFoundException, DissolutionNotLinkedToTransactionException {
        var dissolution = dissolutionService.getDissolutionForTransaction(transaction, dissolutionId);
        var company = dissolution.getCompany();
        var applicationType = dissolution.getData().getApplication().getType();

        Cost cost = new Cost();
        cost.setAmount(feeConfig.getClosingPounds());
        cost.setClassOfPayment(List.of(PAYMENT_CLASS_OF_PAYMENT));
        cost.setAvailablePaymentMethods(List.of(PAYMENT_AVAILABLE_PAYMENT_METHOD));
        cost.setDescription(String.format(PAYMENT_DESCRIPTION, company.getName(), company.getNumber()));
        cost.setDescriptionIdentifier(PAYMENT_DESCRIPTION_IDENTIFIER);
        cost.setDescriptionValues(Collections.singletonMap("Key", "Value"));
        cost.setKind(RESOURCE_KIND);
        cost.setResourceKind(PAYMENT_SESSION);
        cost.setProductType(applicationType.getValue());

        return cost;
    }
}