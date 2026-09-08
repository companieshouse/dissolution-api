package uk.gov.companieshouse.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.model.domain.DissolutionCost;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.companieshouse.model.Constants.PAYMENT_AVAILABLE_PAYMENT_METHOD;
import static uk.gov.companieshouse.model.Constants.PAYMENT_CLASS_OF_PAYMENT;
import static uk.gov.companieshouse.model.Constants.PAYMENT_DESCRIPTION;
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
    void maps_dissolutionCost_to_cost() {
        var dissolutionCost = new DissolutionCost(AMOUNT, COMPANY_NAME, COMPANY_NUMBER, APPLICATION_TYPE);

        var actualCost = costMapper.mapToCost(dissolutionCost);

        assertThat(actualCost).isNotNull();
        assertThat(actualCost.getAmount()).isEqualTo(AMOUNT);
        assertThat(actualCost.getDescription()).isEqualTo(String.format(PAYMENT_DESCRIPTION, COMPANY_NAME, COMPANY_NUMBER));
        assertThat(actualCost.getDescriptionIdentifier()).isEqualTo(PAYMENT_DESCRIPTION_IDENTIFIER);
        assertThat(actualCost.getDescriptionValues())
                .containsEntry(DESCRIPTION_KEY, DESCRIPTION_VALUE);
        assertThat(actualCost.getAvailablePaymentMethods()).containsExactly(PAYMENT_AVAILABLE_PAYMENT_METHOD);
        assertThat(actualCost.getClassOfPayment()).containsExactly(PAYMENT_CLASS_OF_PAYMENT);
        assertThat(actualCost.getKind()).isEqualTo("dissolution");
        assertThat(actualCost.getResourceKind()).isEqualTo("payment-session#payment-session");
        assertThat(actualCost.getProductType()).isEqualTo(APPLICATION_TYPE);
    }
}
