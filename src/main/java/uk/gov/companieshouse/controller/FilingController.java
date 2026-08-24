package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.BadRequestException;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.InternalServerErrorException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.logging.Logger;
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

    @Operation(summary = "Get Dissolution filing", tags = "Dissolution")
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
            @PathVariable(DISSOLUTION_ID_KEY) final String dissolutionId) {
        var logCtx = new HashMap<String, Object>();
        logCtx.put(TRANSACTION_ID_KEY, transactionId);
        logCtx.put(DISSOLUTION_ID_KEY, dissolutionId);

        if (!transaction.getStatus().equals(TransactionStatus.CLOSED)) {
            logCtx.put("transaction_status", transaction.getStatus());
            logger.infoContext(requestId, "Failed to generate dissolution filing as transaction is not CLOSED", logCtx);
            throw new ConflictException("Rejected filings request due to invalid transaction status");
        }

        try {
            FilingApi filing = filingService.generateDissolutionFiling(transaction, dissolutionId);
            return new FilingApi[] { filing };
        } catch (DissolutionNotLinkedToTransactionException e) {
            throw new BadRequestException(e.getMessage());
        } catch (DissolutionNotFoundException e) {
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to generate dissolution filing", e);
        }
    }
}
