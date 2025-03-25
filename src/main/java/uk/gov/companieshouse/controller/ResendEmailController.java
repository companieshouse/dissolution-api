package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;

import static java.lang.String.format;

@RestController
@RequestMapping("/dissolution-request/{company-number}/resend-email/{email-address}")
public class ResendEmailController {

    private final DissolutionEmailService emailService;
    private final DissolutionRepository dissolutionRepository;
    private final Logger logger;

    public ResendEmailController(
            DissolutionEmailService emailService,
            DissolutionRepository dissolutionRepository,
            Logger logger) {

        this.emailService = emailService;
        this.dissolutionRepository = dissolutionRepository;
        this.logger = logger;
    }

    @Operation(summary = "Resend signatory email", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email resent"),
            @ApiResponse(responseCode = "404", description = "Dissolution not found")
    })

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void resendSignatoryEmail(
            @PathVariable("company-number") final String companyNumber,
            @PathVariable("email-address") final String emailAddress) {

        try {
            logger.info(format("*** Company Number: %s, Email Address: %s", companyNumber, emailAddress));

            final Dissolution dissolution = dissolutionRepository.findByCompanyNumber(companyNumber).orElseThrow(DissolutionNotFoundException::new);
            emailService.notifySignatoryToSign(dissolution, emailAddress);
        } catch(Exception e) {
            throw new NotFoundException();
        }
    }
}
