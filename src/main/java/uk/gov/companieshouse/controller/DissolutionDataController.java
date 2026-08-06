package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

@SuppressWarnings("UastIncorrectHttpHeaderInspection")
@RestController
@RequestMapping("/dissolution/{company-number}")
public class DissolutionDataController {

    private static final String ERROR_DISSOLUTION_NOT_FOUND = "Pending or draft dissolution application not found for company number %s and user %s";
    private final DissolutionService dissolutionService;

    public DissolutionDataController(DissolutionService dissolutionService) {
        this.dissolutionService = dissolutionService;
    }

    @Operation(summary = "Get Pending or Draft Dissolution Application", tags = "Dissolution (Transaction Model)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending or Draft Dissolution Application found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pending or Draft Dissolution Application not found")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public DissolutionGetResponse getDissolutionApplication(@RequestHeader("ERIC-identity") String userId,
                                                            @PathVariable("company-number") final String companyNumber) {
        return dissolutionService
                .getPendingOrDraftDissolution(userId, companyNumber)
                .orElseThrow(() -> new NotFoundException(String.format(ERROR_DISSOLUTION_NOT_FOUND, companyNumber, userId)));
    }
}
