package uk.gov.companieshouse.service.cost;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.config.FeeConfig;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.fixtures.DissolutionFixtures;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.model.Constants.PAYMENT_AVAILABLE_PAYMENT_METHOD;
import static uk.gov.companieshouse.model.Constants.PAYMENT_CLASS_OF_PAYMENT;
import static uk.gov.companieshouse.model.Constants.PAYMENT_DESCRIPTION_IDENTIFIER;
import static uk.gov.companieshouse.model.Constants.PAYMENT_ITEM_KIND;
import static uk.gov.companieshouse.model.Constants.PAYMENT_RESOURCE_KIND;

@ExtendWith(MockitoExtension.class)
public class CostServiceTest {
    @Mock
    private DissolutionService dissolutionService;

    @Mock
    private FeeConfig feeConfig;

    @InjectMocks
    private CostService costService;

    @Test
    void givenGetCostsCalled_returnsCost_whenvalidDissolutionId() throws DissolutionNotFoundException {
        final DissolutionGetResponse dissolution = DissolutionFixtures.generateDissolutionGetResponse();
        dissolution.setCompanyName("Test Company");
        dissolution.setCompanyNumber("12345678");
        String dissolutionId = "987654321";
        dissolution.setApplicationType(ApplicationType.DS01);
        when(dissolutionService.getById(dissolutionId)).thenReturn(Optional.of(dissolution));
        when(feeConfig.getClosingPounds()).thenReturn("10.00");

        Cost actualCost = costService.getCosts(dissolutionId);

        assertNotNull(actualCost);
        assertEquals("10.00", actualCost.getAmount());
        assertEquals("Apply to strike off and dissolve a company: Test Company (12345678)", actualCost.getDescription());
        assertEquals(PAYMENT_DESCRIPTION_IDENTIFIER, actualCost.getDescriptionIdentifier());
        assertNotNull(actualCost.getDescriptionValues());
        assertTrue(actualCost.getDescriptionValues().isEmpty());
        assertEquals(PAYMENT_AVAILABLE_PAYMENT_METHOD, actualCost.getAvailablePaymentMethods().getFirst());
        assertEquals(PAYMENT_CLASS_OF_PAYMENT, actualCost.getClassOfPayment().getFirst());
        assertEquals(PAYMENT_ITEM_KIND, actualCost.getKind());
        assertEquals(PAYMENT_RESOURCE_KIND, actualCost.getResourceKind());
        assertEquals(ApplicationType.DS01.getValue(), actualCost.getProductType());
    }

    @Test
    void givenGetCostsCalled_throwsDissolutionNotFoundException_whenInvalidDissolutionId() {
        String dissolutionId = "invalid-id";
        when(dissolutionService.getById(dissolutionId)).thenReturn(Optional.empty());

        assertThrows(DissolutionNotFoundException.class, () -> costService.getCosts(dissolutionId));
    }
}
