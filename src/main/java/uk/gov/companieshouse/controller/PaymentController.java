package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.exception.BadRequestException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.EmailSendException;
import uk.gov.companieshouse.exception.InternalServerErrorException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentGetResponse;
import uk.gov.companieshouse.model.dto.payment.PaymentPatchRequest;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.model.enums.PaymentStatus;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.PaymentValidator;
import uk.gov.companieshouse.service.payment.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/dissolution-request/{application-reference}/payment")
public class PaymentController {

    private final DissolutionService dissolutionService;
    private final PaymentService paymentService;
    private final PaymentValidator paymentValidator;

    public PaymentController(
            DissolutionService dissolutionService,
            PaymentService paymentService,
            PaymentValidator paymentValidator
    ) {
        super();
        this.dissolutionService = dissolutionService;
        this.paymentService = paymentService;
        this.paymentValidator = paymentValidator;
    }

    @Operation(summary = "Get Payment UI Data", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment UI Data Found"),
            @ApiResponse(responseCode = "404", description = "Dissolution Application not found")
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public PaymentGetResponse getPaymentUIData(@PathVariable("application-reference") final String applicationReference) {
        DissolutionGetResponse dissolutionInfo = dissolutionService
                .getByApplicationReference(applicationReference)
                .orElseThrow(NotFoundException::new);

        return paymentService.get(dissolutionInfo);
    }

    @Operation(summary = "Patch Payment Status", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Payment Status Updated"),
            @ApiResponse(responseCode = "400", description = "Wrong status of Dissolution Application"),
            @ApiResponse(responseCode = "404", description = "Dissolution Application not found")
    })
    @PatchMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchPaymentData(
            @PathVariable("application-reference") final String applicationReference,
            @Valid @RequestBody final PaymentPatchRequest body
    ) {
        DissolutionGetResponse dissolutionInfo = dissolutionService
                .getByApplicationReference(applicationReference)
                .orElseThrow(NotFoundException::new);

        if (dissolutionInfo.getApplicationStatus() != ApplicationStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Dissolution status is not " + ApplicationStatus.PENDING_PAYMENT.getValue());
        }

        paymentValidator
                .checkBusinessRules(body)
                .ifPresent(error -> {
                    throw new BadRequestException(error);
                });

        if (PaymentStatus.PAID.equals(body.getStatus())) {
            try {
                dissolutionService.handlePayment(body, applicationReference);
            } catch (EmailSendException e) {
                throw new InternalServerErrorException(e.getMessage());
            } catch (DissolutionNotFoundException e) {
                throw new NotFoundException();
            }
        } else {
            try {
                dissolutionService.setPaymentReference(body.getPaymentReference(), applicationReference);
            } catch (DissolutionNotFoundException e) {
                throw new NotFoundException();
            }
        }
    }
}
