package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.db.payment.PaymentInformation;
import uk.gov.companieshouse.model.dto.payment.PaymentDescriptionValues;
import uk.gov.companieshouse.model.dto.payment.PaymentGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentItem;
import uk.gov.companieshouse.model.dto.payment.PaymentLinks;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;
import uk.gov.companieshouse.model.enums.ApplicationType;
import uk.gov.companieshouse.model.enums.PaymentMethod;
import uk.gov.companieshouse.model.enums.PaymentStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentFixtures {
    public static PaymentGetResponse generatePaymentGetResponse(String eTag, String companyNumber) {
        PaymentGetResponse response = new PaymentGetResponse();

        PaymentLinks paymentLinks = new PaymentLinks();
        paymentLinks.setSelf("/dissolution-request/" + companyNumber + "/payment");
        paymentLinks.setDissolutionRequest("/dissolution-request/" + companyNumber);

        PaymentItem item = new PaymentItem();
        item.setDescription("Dissolution application");
        item.setDescriptionIdentifier("Dissolution application");
        item.setDescriptionValues(new PaymentDescriptionValues());
        item.setProductType(ApplicationType.DS01);
        item.setAmount("8");
        item.setAvailablePaymentMethods(List.of("credit-card"));
        item.setClassOfPayment(List.of("data-maintenance"));
        item.setKind("dissolution-request#payment-details");
        item.setResourceKind("dissolution-request#dissolution-request");

        response.setETag(eTag);
        response.setKind("dissolution-request#payment");
        response.setLinks(paymentLinks);
        response.setItems(List.of(item));

        return response;
    }

    public static PaymentPatchRequest generatePaymentPatchRequest() {
        PaymentPatchRequest paymentPatchRequest = new PaymentPatchRequest();

        paymentPatchRequest.setPaidAt(Timestamp.valueOf(LocalDateTime.now()));
        paymentPatchRequest.setPaymentReference("ABCDEFGH1234567");
        paymentPatchRequest.setStatus(PaymentStatus.PAID);

        return paymentPatchRequest;
    }

    public static PaymentInformation generatePaymentInformation() {
        PaymentInformation paymentInformation = new PaymentInformation();

        paymentInformation.setMethod(PaymentMethod.CREDIT_CARD);
        paymentInformation.setReference("ABCDEFGH1234567");
        paymentInformation.setDateTime(LocalDateTime.now());

        return paymentInformation;
    }

    public static RefundRequest generateRefundRequest() {
        final RefundRequest request = new RefundRequest();

        request.setAmount(800);

        return request;
    }

    public static RefundResponse generateRefundResponse() {
        final RefundResponse response = new RefundResponse();

        response.setAmount(800);
        response.setRefundId("REF123");
        response.setCreatedDateTime(LocalDateTime.now().toString());
        response.setStatus("available");

        return response;
    }
}
