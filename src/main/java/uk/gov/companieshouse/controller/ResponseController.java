package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.model.dto.chips.ChipsResponseCreateRequest;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.chips.ChipsResponseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/dissolution-request/response")
public class ResponseController {

    private final DissolutionService dissolutionService;

    private final ChipsResponseService chipsResponseService;

    public ResponseController(DissolutionService dissolutionService, ChipsResponseService chipsResponseService) {
        this.dissolutionService = dissolutionService;
        this.chipsResponseService = chipsResponseService;
    }

    @Operation(summary = "Save and notify the applicant the outcome of the dissolution application", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dissolution application outcome saved and notified"),
            @ApiResponse(responseCode = "404", description = "Dissolution application not found")
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public void postDissolutionApplicationOutcome(@Valid @RequestBody final ChipsResponseCreateRequest body) {
        if (!dissolutionService.doesDissolutionRequestExistForCompanyByApplicationReference(body.getSubmissionReference())) {
            throw new NotFoundException();
        }

        try {
            chipsResponseService.saveAndNotifyDissolutionApplicationOutcome(body);
        } catch (DissolutionNotFoundException e) {
            throw new NotFoundException();
        }

    }
}
