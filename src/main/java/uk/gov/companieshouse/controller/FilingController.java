package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.ServiceUnavailableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.LogContext;
import uk.gov.companieshouse.logging.util.LogHelper;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;
import uk.gov.companieshouse.service.dissolution.chips.DissolutionChipsService;
import uk.gov.companieshouse.service.transaction.FilingService;

import java.util.HashMap;

import static uk.gov.companieshouse.model.Constants.*;

@RestController
@RequestMapping("/private/transactions/{transaction_id}/dissolution/{company_number}/filings")
public class FilingController {

    private final FilingService filingService;
    private final Logger logger;

    public FilingController(FilingService filingService, Logger logger) {
        this.filingService = filingService;
        this.logger = logger;
    }

    @Operation(summary = "Submit Dissolution Requests to CHIPS", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dissolutions sent to CHIPS"),
            @ApiResponse(responseCode = "503", description = "CHIPS is not available")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void submitDissolutionsToChips() {
        if (!service.isAvailable()) {
            throw new ServiceUnavailableException();
        }

        service.submitDissolutionsToChips();
    }

    @Operation(summary = "Get Dissolution filing", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dissolutions sent to CHIPS"),
            @ApiResponse(responseCode = "400", description = "Company Cannot Be Closed"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<FilingApi[]> getFiling(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @RequestHeader(HEADER_ERIC_REQUEST_ID) String requestId,
            @PathVariable(TRANSACTION_ID_KEY) String transactionId,
            @PathVariable(COMPANY_NUMBER_KEY) final String companyNumber) {

        var logCtx = new HashMap<String, Object>();
        logCtx.put(TRANSACTION_ID_KEY, transactionId);
        logCtx.put(COMPANY_NUMBER_KEY, companyNumber);

        logger.infoContext(requestId, "Calling service to retrieve filing", logCtx);
        logger.error
        try {
            FilingApi filing = filingService.generateDissolutionFiling(companyNumber, transaction);
            return ResponseEntity.ok(new FilingApi[] { filing });
        } catch (DissolutionNotLinkedToTransactionException e) {
            logger.errorContext(requestId, e);
            return ResponseEntity.badRequest().build();
        } catch (DissolutionNotFoundException e) {
            logger.errorContext(requestId, e.getMessage(), e, logCtx);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.errorContext(requestId, e.getMessage(), e, logCtx);
            return ResponseEntity.internalServerError().build();
        }
    }
}
