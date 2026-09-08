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
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.CostMapper;
import uk.gov.companieshouse.mapper.ValidationStatusResponseMapper;
import uk.gov.companieshouse.service.cost.CostService;
import uk.gov.companieshouse.service.transaction.FilingService;

import java.util.HashMap;
import java.util.List;

import static uk.gov.companieshouse.model.Constants.DISSOLUTION_ID_KEY;
import static uk.gov.companieshouse.model.Constants.HEADER_ERIC_REQUEST_ID;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_ID_KEY;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_KEY;

@RestController
public class FilingController {

	private final FilingService filingService;
	private final ValidationStatusResponseMapper validationStatusResponseMapper;
	private final CostService costService;
	private final CostMapper costMapper;
	private final Logger logger;

	public FilingController(FilingService filingService, ValidationStatusResponseMapper validationStatusResponseMapper,
	                        CostService costService, CostMapper costMapper, Logger logger) {
		this.filingService = filingService;
		this.validationStatusResponseMapper = validationStatusResponseMapper;
		this.costService = costService;
		this.costMapper = costMapper;
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
	@GetMapping("/private/transactions/{transaction_id}/dissolution/{dissolution_id}/filings")
	@ResponseStatus(HttpStatus.OK)
	public FilingApi[] getFiling(
			@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
			@RequestHeader(HEADER_ERIC_REQUEST_ID) String requestId,
			@PathVariable(TRANSACTION_ID_KEY) String transactionId,
			@PathVariable(DISSOLUTION_ID_KEY) final String dissolutionId) {
		var logCtx = new HashMap<String, Object>();
		logCtx.put(TRANSACTION_ID_KEY, transactionId);
		logCtx.put(DISSOLUTION_ID_KEY, dissolutionId);
		logger.infoContext(requestId, "Attempting to generate dissolution filing", logCtx);

		FilingApi filing = filingService.generateDissolutionFiling(transaction, dissolutionId);
		return new FilingApi[]{filing};
	}

	@Operation(summary = "Get Dissolution filing validation status", tags = "Dissolution (Transaction Model)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dissolution validation status determined successfully"),
			@ApiResponse(responseCode = "400", description = "Dissolution not linked to transaction"),
			@ApiResponse(responseCode = "404", description = "Dissolution not found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error")
	})
	@GetMapping("/transactions/{transaction_id}/dissolution/{dissolution_id}/validation-status")
	@ResponseStatus(HttpStatus.OK)
	public ValidationStatusResponse getValidationStatus(
			@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
			@RequestHeader(HEADER_ERIC_REQUEST_ID) String requestId,
			@PathVariable(TRANSACTION_ID_KEY) String transactionId,
			@PathVariable(DISSOLUTION_ID_KEY) final String dissolutionId) {
		var logCtx = new HashMap<String, Object>();
		logCtx.put(TRANSACTION_ID_KEY, transactionId);
		logCtx.put(DISSOLUTION_ID_KEY, dissolutionId);
		logger.infoContext(requestId, "Attempting to validate dissolution for filing", logCtx);

		var validationResult = filingService.validateForFiling(transaction, dissolutionId);
		return validationStatusResponseMapper.mapToValidationStatusResponse(validationResult);
	}

	@Operation(summary = "Get Dissolution Costs", tags = "Dissolution")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Costs found"),
			@ApiResponse(responseCode = "400", description = "Dissolution not linked to transaction"),
			@ApiResponse(responseCode = "404", description = "Dissolution not found")
	})
	@GetMapping("/transactions/{transaction_id}/dissolution/{dissolution_id}/costs")
	@ResponseStatus(HttpStatus.OK)
	public List<Cost> getCosts(
			@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
			@PathVariable(TRANSACTION_ID_KEY) String transactionId,
			@PathVariable(DISSOLUTION_ID_KEY) String dissolutionId,
			@RequestHeader(HEADER_ERIC_REQUEST_ID) String requestId) {

        logger.info("Getting costs for transaction: " + transactionId + ", dissolution: " + dissolutionId + ", requestId: " + requestId);

		var dissolutionCost = costService.getCosts(transaction, dissolutionId);
		return List.of(costMapper.mapToCost(dissolutionCost));
	}
}
