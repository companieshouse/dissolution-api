package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.exception.DissolutionApplicationWrongStatusException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.PaymentStatus;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.payment.PaymentService;

import javax.validation.Valid;

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

    @Operation(summary = "Patch Payment Status", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment Status Updated"),
            @ApiResponse(responseCode = "400", description = "Wrong status of Dissolution Application"),
            @ApiResponse(responseCode = "404", description = "Dissolution Application not found")
    })
    @PatchMapping()
    @ResponseStatus(HttpStatus.OK)
    public void patchPaymentData(@PathVariable("company-number") final String companyNumber,
                                 @Valid @RequestBody final PaymentPatchRequest body) {

        logger.debug("[PATCH] Submitting payment data update request for company number {}", companyNumber);

        DissolutionGetResponse dissolutionInfo = dissolutionService
                .get(companyNumber)
                .orElseThrow(DissolutionNotFoundException::new);

        if (dissolutionInfo.getApplicationStatus() != ApplicationStatus.PENDING_PAYMENT) {
            throw new DissolutionApplicationWrongStatusException();
        }

        if (PaymentStatus.PAID.equals(body.getStatus())) {
            dissolutionService.updatePaymentStatus(body, companyNumber);
        }
    }
}
