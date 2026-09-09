package uk.gov.companieshouse.mapper;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.model.domain.DissolutionCost;

import java.util.Collections;
import java.util.List;

import static uk.gov.companieshouse.model.Constants.PAYMENT_AVAILABLE_PAYMENT_METHOD;
import static uk.gov.companieshouse.model.Constants.PAYMENT_CLASS_OF_PAYMENT;
import static uk.gov.companieshouse.model.Constants.PAYMENT_DESCRIPTION;
import static uk.gov.companieshouse.model.Constants.PAYMENT_DESCRIPTION_IDENTIFIER;

@Component
public class CostMapper {

    private static final String PAYMENT_SESSION = "payment-session#payment-session";
    private static final String RESOURCE_KIND = "dissolution";

    public Cost mapToCost(DissolutionCost dissolutionCost) {
        Cost cost = new Cost();
        cost.setAmount(dissolutionCost.amount());
        cost.setClassOfPayment(List.of(PAYMENT_CLASS_OF_PAYMENT));
        cost.setAvailablePaymentMethods(List.of(PAYMENT_AVAILABLE_PAYMENT_METHOD));
        cost.setDescription(String.format(PAYMENT_DESCRIPTION, dissolutionCost.companyName(), dissolutionCost.companyNumber()));
        cost.setDescriptionIdentifier(PAYMENT_DESCRIPTION_IDENTIFIER);
        cost.setDescriptionValues(Collections.singletonMap("Key", "Value"));
        cost.setKind(RESOURCE_KIND);
        cost.setResourceKind(PAYMENT_SESSION);
        cost.setProductType(dissolutionCost.applicationType());

        return cost;
    }
}
