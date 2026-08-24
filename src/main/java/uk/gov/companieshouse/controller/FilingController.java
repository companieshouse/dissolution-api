package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;
import uk.gov.companieshouse.service.transaction.FilingService;

import java.util.HashMap;

import static uk.gov.companieshouse.model.Constants.DISSOLUTION_ID_KEY;
import static uk.gov.companieshouse.model.Constants.HEADER_ERIC_REQUEST_ID;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_KEY;

@RestController
@RequestMapping("/private/transactions/{transaction_id}/dissolution/{dissolution_id}/filings")
public class FilingController {

    private final FilingService filingService;
    private final Logger logger;

    public FilingController(FilingService filingService, Logger logger) {
        this.filingService = filingService;
        this.logger = logger;
    }

    @Operation(summary = "Get Dissolution filing", tags = "Dissolution (Transaction Model)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dissolution filing generated successfully"),
            @ApiResponse(responseCode = "400", description = "Dissolution not linked to transaction"),
            @ApiResponse(responseCode = "404", description = "Dissolution not found"),
            @ApiResponse(responseCode = "409", description = "Invalid transaction status"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public FilingApi[] getFiling(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @RequestHeader(HEADER_ERIC_REQUEST_ID) String requestId,
            @PathVariable(TRANSACTION_ID_KEY) String transactionId,
            @PathVariable(DISSOLUTION_ID_KEY) final String dissolutionId,
            HttpServletRequest request) {
        final var passThroughHeader = request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader());

        var logCtx = new HashMap<String, Object>();
        logCtx.put(TRANSACTION_ID_KEY, transactionId);
        logCtx.put(DISSOLUTION_ID_KEY, dissolutionId);
        logger.infoContext(requestId, "Attempting to generate dissolution filing", logCtx);

        FilingApi filing = filingService.generateDissolutionFiling(transaction, dissolutionId, passThroughHeader);
        return new FilingApi[]{filing};
    }
}
