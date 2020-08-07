package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.exception.ServiceUnavailableException;
import uk.gov.companieshouse.service.dissolution.chips.DissolutionChipsService;

@RestController
@RequestMapping("/dissolution-request/submit")
public class SubmitController {

    private final Logger logger = LoggerFactory.getLogger(SubmitController.class);

    private final DissolutionChipsService service;

    public SubmitController(DissolutionChipsService service) {
        this.service = service;
    }

    @Operation(summary = "Submit Dissolution Requests to CHIPS", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Dissolutions sent to CHIPS"),
            @ApiResponse(responseCode = "503", description = "CHIPS is not available")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void submitDissolutionsToChips() {
        logger.info("[POST] Submitting dissolutions to CHIPS");

        if (!service.isAvailable()) {
            throw new ServiceUnavailableException();
        }

        service.submitDissolutionsToChips();
    }
}
