package uk.gov.companieshouse.fixtures;

import uk.gov.companieshouse.model.dto.payment.*;
import uk.gov.companieshouse.model.enums.PaymentStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentFixtures {
    public static PaymentGetResponse generatePaymentGetResponse(String eTag, String companyNumber) {
        PaymentGetResponse response = new PaymentGetResponse() {{
            setETag(eTag);
            setKind("dissolution-request#payment");
            setLinks(new PaymentLinks() {{
                setSelf("/dissolution-request/" + companyNumber + "/payment");
                setDissolutionRequest("/dissolution-request/" + companyNumber);
            }});
        }};

        PaymentItem item = new PaymentItem() {{
            setDescription("Dissolution application");
            setDescriptionIdentifier("Dissolution application");
            setDescriptionValues(new PaymentDescriptionValues());
            setProductType("Dissolution application");
            setAmount("8");
            setAvailablePaymentMethods(List.of("credit-card"));
            setClassOfPayment(List.of("data-maintenance"));
            setKind("dissolution-request#payment-details");
            setResourceKind("dissolution-request#dissolution-request");
        }};
        response.setItems(List.of(item));

        return response;
    }

    public static PaymentPatchRequest generatePaymentPatchRequest() {
        return new PaymentPatchRequest() {{
            setPaidAt(Timestamp.valueOf(LocalDateTime.now()));
            setPaymentReference("ABCDEFGH1234567");
            setStatus(PaymentStatus.PAID);
        }};
    }
}
