package uk.gov.companieshouse.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.model.dto.PaymentGetResponse;
import uk.gov.companieshouse.model.dto.PaymentLinks;

@Service
public class PaymentService {

    public PaymentGetResponse get(String eTag, String companyNumber) {
        PaymentGetResponse response = new PaymentGetResponse();
        response.setETag(eTag);
        response.setKind("dissolution-request#payment");
        response.setLinks(new PaymentLinks() {{
            setSelf("/dissolution-request/{companyNumber}/payment");
            setDissolutionRequest("/dissolution-request/{companyNumber}");
        }});

        response.setItems();

        return response;
    }
}
