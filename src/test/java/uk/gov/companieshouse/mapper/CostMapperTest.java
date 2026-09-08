package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.domain.DissolutionCost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.model.Constants.PAYMENT_AVAILABLE_PAYMENT_METHOD;
import static uk.gov.companieshouse.model.Constants.PAYMENT_CLASS_OF_PAYMENT;
import static uk.gov.companieshouse.model.Constants.PAYMENT_DESCRIPTION_IDENTIFIER;

class CostMapperTest {

    private static final String DESCRIPTION_KEY = "Key";
    private static final String DESCRIPTION_VALUE = "Value";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String COMPANY_NAME = "Test Company";
    private static final String AMOUNT = "10.00";
    private static final String APPLICATION_TYPE = "dissolution-strike-off";

    private final CostMapper costMapper = new CostMapper();

    @Test
    void mapToCost_mapsDissolutionCostToCost() {
        var dissolutionCost = new DissolutionCost(AMOUNT, COMPANY_NAME, COMPANY_NUMBER, APPLICATION_TYPE);

        var actualCost = costMapper.mapToCost(dissolutionCost);

        assertNotNull(actualCost);
        assertEquals(AMOUNT, actualCost.getAmount());
        assertEquals(String.format("Apply to strike off and dissolve a company: %s (%s)", COMPANY_NAME, COMPANY_NUMBER), actualCost.getDescription());
        assertEquals(PAYMENT_DESCRIPTION_IDENTIFIER, actualCost.getDescriptionIdentifier());
        assertNotNull(actualCost.getDescriptionValues());
        assertTrue(actualCost.getDescriptionValues().containsKey(DESCRIPTION_KEY));
        assertEquals(DESCRIPTION_VALUE, actualCost.getDescriptionValues().get(DESCRIPTION_KEY));
        assertEquals(PAYMENT_AVAILABLE_PAYMENT_METHOD, actualCost.getAvailablePaymentMethods().getFirst());
        assertEquals(PAYMENT_CLASS_OF_PAYMENT, actualCost.getClassOfPayment().getFirst());
        assertEquals("dissolution", actualCost.getKind());
        assertEquals("payment-session#payment-session", actualCost.getResourceKind());
        assertEquals(APPLICATION_TYPE, actualCost.getProductType());
    }
}
