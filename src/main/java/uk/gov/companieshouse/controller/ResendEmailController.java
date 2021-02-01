package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;

@RestController
@RequestMapping("/dissolution-request/{company-number}/resend-email/{email-address}")
public class ResendEmailController {

    private final DissolutionEmailService emailService;
    private final DissolutionRepository repository;

    public ResendEmailController(
            DissolutionEmailService emailService,
            DissolutionRepository repository) {

        this.emailService = emailService;
        this.repository = repository;
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
            final Dissolution dissolution = repository.findByCompanyNumber(companyNumber).orElseThrow(DissolutionNotFoundException::new);
            emailService.notifySignatoryToSign(dissolution, emailAddress);
        } catch(Exception e) {
            throw new NotFoundException();
        }
    }
}
