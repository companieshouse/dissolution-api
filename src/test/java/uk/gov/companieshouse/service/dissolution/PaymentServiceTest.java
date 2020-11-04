package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentGetResponse;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.payment.PaymentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionGetResponse;
import static uk.gov.companieshouse.model.Constants.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService service;

    @Test
    public void get_getsPaymentUIData_returnsGetResponse() {
        String companyNumber = "12345678";
        String applicationReference = "ABC123";

        DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setCompanyNumber(companyNumber);
        dissolutionGetResponse.setApplicationReference(applicationReference);
        dissolutionGetResponse.setETag("WATERMELONSAREGREAT12345678THEYREALLYARE");
        dissolutionGetResponse.setApplicationType(ApplicationType.DS01);

        final PaymentGetResponse result = service.get(dissolutionGetResponse);

        assertEquals(dissolutionGetResponse.getETag(), result.getETag());
        assertEquals(PAYMENT_KIND, result.getKind());
        assertEquals(companyNumber, result.getCompanyNumber());
        assertEquals("/dissolution-request/" + applicationReference + "/payment", result.getLinks().getSelf());
        assertEquals("/dissolution-request/" + companyNumber, result.getLinks().getDissolutionRequest());
        assertEquals(PAYMENT_DESCRIPTION, result.getItems().get(0).getDescription());
        assertEquals(PAYMENT_DESCRIPTION_IDENTIFIER, result.getItems().get(0).getDescriptionIdentifier());
        assertNotNull(result.getItems().get(0).getDescriptionValues());
        assertEquals(PAYMENT_AMOUNT, result.getItems().get(0).getAmount());
        assertEquals(PAYMENT_AVAILABLE_PAYMENT_METHOD, result.getItems().get(0).getAvailablePaymentMethods().get(0));
        assertEquals(PAYMENT_CLASS_OF_PAYMENT, result.getItems().get(0).getClassOfPayment().get(0));
        assertEquals(PAYMENT_ITEM_KIND, result.getItems().get(0).getKind());
        assertEquals(PAYMENT_RESOURCE_KIND, result.getItems().get(0).getResourceKind());
    }

    @Test
    public void get_getsPaymentUIData_returnsProperCodeForDS01() {
        String companyNumber = "12345678";

        DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setCompanyNumber(companyNumber);
        dissolutionGetResponse.setETag("WATERMELONSAREGREAT12345678THEYREALLYARE");
        dissolutionGetResponse.setApplicationType(ApplicationType.DS01);

        final PaymentGetResponse result = service.get(dissolutionGetResponse);

        assertEquals(ApplicationType.DS01, result.getItems().get(0).getProductType());
    }

    @Test
    public void get_getsPaymentUIData_returnsProperCodeForLLDS01() {
        String companyNumber = "12345678";

        DissolutionGetResponse dissolutionGetResponse = generateDissolutionGetResponse();
        dissolutionGetResponse.setCompanyNumber(companyNumber);
        dissolutionGetResponse.setETag("WATERMELONSAREGREAT12345678THEYREALLYARE");
        dissolutionGetResponse.setApplicationType(ApplicationType.LLDS01);

        final PaymentGetResponse result = service.get(dissolutionGetResponse);

        System.out.println(result.getItems().get(0).getProductType());
        assertEquals(ApplicationType.LLDS01, result.getItems().get(0).getProductType());
    }
}
