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
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.exception.BadRequestException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.DissolutionNotLinkedToTransactionException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.service.cost.CostService;

import java.util.List;

import static uk.gov.companieshouse.model.Constants.*;

@RestController
@RequestMapping("/transactions/{transaction_id}/dissolution/{dissolution_id}/costs")
public class CostController {
    private final CostService costService;
    private final Logger logger;

    public CostController(CostService costService, Logger logger) {
        this.costService = costService;
        this.logger = logger;
    }

    @Operation(summary = "Get Dissolution Costs", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Costs found"),
            @ApiResponse(responseCode = "400", description = "Dissolution not linked to transaction"),
            @ApiResponse(responseCode = "404", description = "Dissolution not found")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Cost> getCosts(
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @PathVariable(TRANSACTION_ID_KEY) String transactionId,
            @PathVariable(DISSOLUTION_ID_KEY) String dissolutionId,
            @RequestHeader(HEADER_ERIC_REQUEST_ID) String requestId) {
        logger.info("Getting costs for transaction: " + transactionId + ", dissolution: " + dissolutionId + ", requestId: " + requestId);
        try {
            return List.of(costService.getCosts(transaction, dissolutionId));
        } catch (DissolutionNotFoundException e) {
            throw new NotFoundException();
        } catch (DissolutionNotLinkedToTransactionException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}