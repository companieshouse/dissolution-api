package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.mapper.CostMapper;
import uk.gov.companieshouse.mapper.ValidationStatusResponseMapper;
import uk.gov.companieshouse.model.domain.GenerateFilingCommand;
import uk.gov.companieshouse.model.domain.GetDissolutionCostsCommand;
import uk.gov.companieshouse.model.domain.ValidateFilingCommand;
import uk.gov.companieshouse.service.cost.CostService;
import uk.gov.companieshouse.service.transaction.FilingService;

import java.util.HashMap;
import java.util.List;

import static uk.gov.companieshouse.model.Constants.COMPANY_NUMBER_KEY;
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
	@GetMapping("/private/company/{company-number}/transactions/{transaction_id}/dissolution/filings")
	@ResponseStatus(HttpStatus.OK)
	public FilingApi[] getFiling(
			@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
			@RequestHeader(HEADER_ERIC_REQUEST_ID) String requestId,
			@PathVariable(COMPANY_NUMBER_KEY) final String companyNumber,
			@PathVariable(TRANSACTION_ID_KEY) String transactionId) {
		var logCtx = new HashMap<String, Object>();
		logCtx.put(TRANSACTION_ID_KEY, transactionId);
		logCtx.put(COMPANY_NUMBER_KEY, companyNumber);
		logger.infoContext(requestId, "Attempting to generate dissolution filing", logCtx);

		final var command = new GenerateFilingCommand(transaction, companyNumber, transactionId);
		FilingApi filing = filingService.generateDissolutionFiling(command);
		return new FilingApi[]{filing};
	}

	@Operation(summary = "Get Dissolution filing validation status", tags = "Dissolution (Transaction Model)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Dissolution validation status determined successfully"),
			@ApiResponse(responseCode = "400", description = "Dissolution not linked to transaction"),
			@ApiResponse(responseCode = "404", description = "Dissolution not found"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error")
	})
	@GetMapping("/company/{company-number}/transactions/{transaction_id}/dissolution/validation-status")
	@ResponseStatus(HttpStatus.OK)
	public ValidationStatusResponse getValidationStatus(
			@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
			@RequestHeader(HEADER_ERIC_REQUEST_ID) String requestId,
			@PathVariable(COMPANY_NUMBER_KEY) final String companyNumber,
			@PathVariable(TRANSACTION_ID_KEY) String transactionId) {
		var logCtx = new HashMap<String, Object>();
		logCtx.put(TRANSACTION_ID_KEY, transactionId);
		logCtx.put(COMPANY_NUMBER_KEY, companyNumber);
		logger.infoContext(requestId, "Attempting to validate dissolution for filing", logCtx);

		final var command = new ValidateFilingCommand(transaction, companyNumber, transactionId);
		var validationResult = filingService.validateForFiling(command);
		return validationStatusResponseMapper.mapToValidationStatusResponse(validationResult);
	}

	@Operation(summary = "Get Dissolution Costs", tags = "Dissolution")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Costs found"),
			@ApiResponse(responseCode = "400", description = "Dissolution not linked to transaction"),
			@ApiResponse(responseCode = "404", description = "Dissolution not found")
	})
	@GetMapping("/company/{company-number}/transactions/{transaction_id}/dissolution/costs")
	@ResponseStatus(HttpStatus.OK)
	public List<Cost> getCosts(
			@RequestAttribute(TRANSACTION_KEY) Transaction transaction,
			@PathVariable(COMPANY_NUMBER_KEY) final String companyNumber,
			@PathVariable(TRANSACTION_ID_KEY) String transactionId,
			@RequestHeader(HEADER_ERIC_REQUEST_ID) String requestId) {

		var logCtx = new HashMap<String, Object>();
		logCtx.put(TRANSACTION_ID_KEY, transactionId);
		logCtx.put(COMPANY_NUMBER_KEY, companyNumber);
		logger.infoContext(requestId, "Getting costs for dissolution filing", logCtx);

		final var command = new GetDissolutionCostsCommand(transaction, companyNumber, transactionId);
		var dissolutionCost = costService.getCosts(command);
		return List.of(costMapper.mapToCost(dissolutionCost));
	}
}
