package uk.gov.companieshouse.service.dissolution;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentGetResponse;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.service.payment.PaymentService;
import static uk.gov.companieshouse.fixtures.DissolutionFixtures.generateDissolutionGetResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.companieshouse.model.Constants.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService service;

    @Test
    public void get_getsPaymentUIData_returnsGetResponse() {
        final DissolutionGetResponse response = generateDissolutionGetResponse();
        final String companyNumber = "12345678";
        final String comapnyName = "A Company";
        final String eTag = "WATERMELONSAREGREAT12345678THEYREALLYARE";
        final ApplicationType applicationType = ApplicationType.DS01;
        response.setCompanyNumber(companyNumber);
        response.setETag(eTag);
        response.setApplicationType(applicationType);
        response.setCompanyName(comapnyName);
        final String expectedDescription = String.format(PAYMENT_DESCRIPTION, comapnyName, companyNumber);

        final PaymentGetResponse result = service.get(response);

        assertEquals(eTag, result.getETag());
        assertEquals(PAYMENT_KIND, result.getKind());
        assertEquals(companyNumber, result.getCompanyNumber());
        assertEquals("/dissolution-request/" + companyNumber + "/payment", result.getLinks().getSelf());
        assertEquals("/dissolution-request/" + companyNumber, result.getLinks().getDissolutionRequest());
        assertEquals(expectedDescription, result.getItems().get(0).getDescription());
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
        final DissolutionGetResponse response = generateDissolutionGetResponse();
        final String companyNumber = "12345678";
        final String eTag = "WATERMELONSAREGREAT12345678THEYREALLYARE";
        final ApplicationType applicationType = ApplicationType.DS01;
        response.setCompanyNumber(companyNumber);
        response.setETag(eTag);
        response.setApplicationType(applicationType);

        final PaymentGetResponse result = service.get(response);

        assertEquals(ApplicationType.DS01, result.getItems().get(0).getProductType());
    }

    @Test
    public void get_getsPaymentUIData_returnsProperCodeForLLDS01() {
        final DissolutionGetResponse response = generateDissolutionGetResponse();

        final String companyNumber = "12345678";
        final String eTag = "WATERMELONSAREGREAT12345678THEYREALLYARE";
        final ApplicationType applicationType = ApplicationType.LLDS01;
        response.setCompanyNumber(companyNumber);
        response.setETag(eTag);
        response.setApplicationType(applicationType);

        final PaymentGetResponse result = service.get(response);

        System.out.println(result.getItems().get(0).getProductType());
        assertEquals(ApplicationType.LLDS01, result.getItems().get(0).getProductType());
    }
}
