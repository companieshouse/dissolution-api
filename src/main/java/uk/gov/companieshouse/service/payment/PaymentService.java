package uk.gov.companieshouse.service.payment;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentDescriptionValues;
import uk.gov.companieshouse.model.dto.payment.PaymentGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentItem;
import uk.gov.companieshouse.model.dto.payment.PaymentLinks;
import uk.gov.companieshouse.model.enums.ApplicationType;

import java.util.List;

import static uk.gov.companieshouse.model.Constants.*;

@Service
public class PaymentService {

    public PaymentGetResponse get(DissolutionGetResponse dissolution) {
        PaymentGetResponse response = new PaymentGetResponse();
        PaymentLinks paymentLinks = new PaymentLinks();
        paymentLinks.setSelf("/dissolution-request/" + dissolution.getCompanyNumber() + "/payment");
        paymentLinks.setDissolutionRequest("/dissolution-request/" + dissolution.getCompanyNumber());

        PaymentItem item = createPaymentItem(dissolution);

        response.setETag(dissolution.getETag());
        response.setKind(PAYMENT_KIND);
        response.setLinks(paymentLinks);
        response.setCompanyNumber(dissolution.getCompanyNumber());
        response.setItems(List.of(item));

        return response;
    }

    private PaymentItem createPaymentItem(DissolutionGetResponse dissolution) {
        PaymentItem item = new PaymentItem();

        item.setDescription(String.format(PAYMENT_DESCRIPTION, dissolution.getCompanyName(), dissolution.getCompanyNumber()));
        item.setDescriptionIdentifier(PAYMENT_DESCRIPTION_IDENTIFIER);
        item.setDescriptionValues(new PaymentDescriptionValues());
        item.setProductType(dissolution.getApplicationType());
        item.setAmount(PAYMENT_AMOUNT);
        item.setAvailablePaymentMethods(List.of(PAYMENT_AVAILABLE_PAYMENT_METHOD));
        item.setClassOfPayment(List.of(PAYMENT_CLASS_OF_PAYMENT));
        item.setKind(PAYMENT_ITEM_KIND);
        item.setResourceKind(PAYMENT_RESOURCE_KIND);

        return item;
    }
}