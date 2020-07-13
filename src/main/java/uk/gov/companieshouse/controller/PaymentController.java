package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.service.DissolutionService;

import javax.servlet.http.HttpServletRequest;

import static uk.gov.companieshouse.util.EricHelper.getEmail;

@RestController
@RequestMapping("/dissolution-request/{company-number}/payment")
public class PaymentController {
    private final DissolutionService dissolutionService;
    private final Logger logger = LoggerFactory.getLogger(DissolutionController.class);

    public PaymentController(DissolutionService dissolutionService) {
        super();
        this.dissolutionService = dissolutionService;
    }

    @Operation(summary = "Get Payment UI Data", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment UI Data Found"),
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public DissolutionCreateResponse getPaymentUIData(
            @PathVariable("company-number") final String companyNumber,
            HttpServletRequest request) {

        if (dissolutionService.doesDissolutionRequestExistForCompany(companyNumber)) {
            throw new ConflictException();
        }

        logger.debug("[POST] Submitting dissolution request for company number {}", companyNumber);

        return dissolutionService.create(body, companyNumber, userId, request.getRemoteAddr(), getEmail(authorisedUser));
    }
}
