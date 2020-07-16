package uk.gov.companieshouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.model.dto.PaymentGetResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.companieshouse.model.Constants.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService service;

    @Test
    public void get_getsPaymentUIData_returnsGetResponse() throws Exception {
        final String companyNumber = "12345678";
        final String eTag = "WATERMELONSAREGREAT12345678THEYREALLYARE";

        final PaymentGetResponse result = service.get(eTag, companyNumber);

        assertEquals(eTag, result.getETag());
        assertEquals(PAYMENT_KIND, result.getKind());
        assertEquals("/dissolution-request/" + companyNumber + "/payment", result.getLinks().getSelf());
        assertEquals("/dissolution-request/" + companyNumber, result.getLinks().getDissolutionRequest());
        assertEquals(PAYMENT_DESCRIPTION, result.getItems().get(0).getDescription());
        assertEquals(PAYMENT_DESCRIPTION_IDENTIFIER, result.getItems().get(0).getDescriptionIdentifier());
        assertNotNull(result.getItems().get(0).getDescriptionValues());
        assertEquals(PAYMENT_PRODUCT_TYPE, result.getItems().get(0).getProductType());
        assertEquals(PAYMENT_AMOUNT, result.getItems().get(0).getAmount());
        assertEquals(PAYMENT_AVAILABLE_PAYMENT_METHOD, result.getItems().get(0).getAvailablePaymentMethods().get(0));
        assertEquals(PAYMENT_CLASS_OF_PAYMENT, result.getItems().get(0).getClassOfPayment().get(0));
        assertEquals(PAYMENT_ITEM_KIND, result.getItems().get(0).getKind());
        assertEquals(PAYMENT_RESOURCE_KIND, result.getItems().get(0).getResourceKind());
    }
}
