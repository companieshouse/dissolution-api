package uk.gov.companieshouse.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uk.gov.companieshouse.config.PaymentsConfig;
import uk.gov.companieshouse.exception.DocumentRenderException;
import uk.gov.companieshouse.model.dto.payment.RefundRequest;
import uk.gov.companieshouse.model.dto.payment.RefundResponse;

import java.util.Optional;

import static uk.gov.companieshouse.model.Constants.*;

@Service
public class PaymentsClient {

    private static final String REFUNDS_URI = "/payments/{paymentReference}/refunds";

    private final PaymentsConfig config;

    @Autowired
    public PaymentsClient(PaymentsConfig config) {
        this.config = config;
    }

    public Optional<RefundResponse> refundPayment(RefundRequest data, String paymentReference) {
        return Optional
            .ofNullable(
                WebClient
                    .create(config.getPaymentsHost())
                    .post()
                    .uri(REFUNDS_URI, paymentReference)
                    .header(HEADER_AUTHORIZATION, config.getApiKey())
                    .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .body(Mono.just(asJsonString(data)), String.class)
                    .retrieve()
                    .bodyToMono(RefundResponse.class)
                    .block()
            );
    }

    private String asJsonString(RefundRequest data) {
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            throw new DocumentRenderException("Failed to write dissolution certificate data to JSON string");
        }
    }
}
