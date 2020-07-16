package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.model.dto.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.PaymentGetResponse;
import uk.gov.companieshouse.service.DissolutionService;
import uk.gov.companieshouse.service.PaymentService;

@RestController
@RequestMapping("/dissolution-request/{company-number}/payment")
public class PaymentController {
    private final DissolutionService dissolutionService;
    private final PaymentService paymentService;
    private final Logger logger = LoggerFactory.getLogger(DissolutionController.class);

    public PaymentController(DissolutionService dissolutionService, PaymentService paymentService) {
        super();
        this.dissolutionService = dissolutionService;
        this.paymentService = paymentService;
    }

    @Operation(summary = "Get Payment UI Data", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment UI Data Found"),
            @ApiResponse(responseCode = "404", description = "Dissolution Application not found")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public PaymentGetResponse getPaymentUIData(@PathVariable("company-number") final String companyNumber) {

        logger.debug("[GET] Submitting payment UI data request for company number {}", companyNumber);

        DissolutionGetResponse dissolutionInfo = dissolutionService
                .get(companyNumber)
                .orElseThrow(DissolutionNotFoundException::new);

        return paymentService.get(dissolutionInfo.getETag(), companyNumber);
    }
}
