package uk.gov.companieshouse.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.PaymentDescriptionValues;
import uk.gov.companieshouse.model.dto.PaymentGetResponse;
import uk.gov.companieshouse.model.dto.PaymentItem;
import uk.gov.companieshouse.model.dto.PaymentLinks;

import java.util.List;

import static uk.gov.companieshouse.model.Constants.*;

@Service
public class PaymentService {

    public PaymentGetResponse get(String eTag, String companyNumber) {
        PaymentGetResponse response = new PaymentGetResponse() {{
            setETag(eTag);
            setKind(PAYMENT_KIND);
            setLinks(new PaymentLinks() {{
                setSelf("/dissolution-request/" + companyNumber + "/payment");
                setDissolutionRequest("/dissolution-request/" + companyNumber);
            }});
        }};

        PaymentItem item = createPaymentItem();
        response.setItems(List.of(item));

        return response;
    }

    private PaymentItem createPaymentItem() {
        return new PaymentItem() {{
            setDescription(PAYMENT_DESCRIPTION);
            setDescriptionIdentifier(PAYMENT_DESCRIPTION_IDENTIFIER);
            setDescriptionValues(new PaymentDescriptionValues());
            setProductType(PAYMENT_PRODUCT_TYPE);
            setAmount(PAYMENT_AMOUNT);
            setAvailablePaymentMethods(List.of(PAYMENT_AVAILABLE_PAYMENT_METHOD));
            setClassOfPayment(List.of(PAYMENT_CLASS_OF_PAYMENT));
            setKind(PAYMENT_ITEM_KIND);
            setResourceKind(PAYMENT_RESOURCE_KIND);
        }};
    }
}