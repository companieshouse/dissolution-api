package uk.gov.companieshouse.service.payment;

import org.springframework.stereotype.Service;
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

    public PaymentGetResponse get(DissolutionGetResponse dissolutionInfo) {
        PaymentGetResponse response = new PaymentGetResponse();
        PaymentLinks paymentLinks = new PaymentLinks();
        paymentLinks.setSelf("/dissolution-request/" + dissolutionInfo.getApplicationReference() + "/payment");
        paymentLinks.setDissolutionRequest("/dissolution-request/" + dissolutionInfo.getCompanyNumber());

        PaymentItem item = createPaymentItem(dissolutionInfo);

        response.setETag(dissolutionInfo.getETag());
        response.setKind(PAYMENT_KIND);
        response.setLinks(paymentLinks);
        response.setCompanyNumber(dissolutionInfo.getCompanyNumber());
        response.setItems(List.of(item));

        return response;
    }

    private PaymentItem createPaymentItem(DissolutionGetResponse dissolutionInfo) {
        PaymentItem item = new PaymentItem();

        item.setDescription(String.format(PAYMENT_DESCRIPTION, dissolutionInfo.getCompanyName(), dissolutionInfo.getCompanyNumber()));
        item.setDescriptionIdentifier(PAYMENT_DESCRIPTION_IDENTIFIER);
        item.setDescriptionValues(new PaymentDescriptionValues());
        item.setProductType(dissolutionInfo.getApplicationType());
        item.setAmount(PAYMENT_AMOUNT);
        item.setAvailablePaymentMethods(List.of(PAYMENT_AVAILABLE_PAYMENT_METHOD));
        item.setClassOfPayment(List.of(PAYMENT_CLASS_OF_PAYMENT));
        item.setKind(PAYMENT_ITEM_KIND);
        item.setResourceKind(PAYMENT_RESOURCE_KIND);

        return item;
    }
}