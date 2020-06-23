package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.model.dto.CreateDissolutionRequestDTO;
import uk.gov.companieshouse.model.dto.CreateDissolutionResponseDTO;
import uk.gov.companieshouse.service.DissolutionService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dissolution-request/{company-number}")
public class DissolutionController {

    private final DissolutionService dissolutionService;

    public DissolutionController(DissolutionService dissolutionService) {
        this.dissolutionService = dissolutionService;
    }

    @Operation(summary = "Create Dissolution Request", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dissolution Request created"),
            @ApiResponse(responseCode = "400", description = "Invalid request format"),
            @ApiResponse(responseCode = "401", description = "Unauthorised"),
            @ApiResponse(responseCode = "409", description = "Dissolution Request already exists for company"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CreateDissolutionResponseDTO> submitDissolutionRequest(
        @RequestHeader("ERIC-identity") String userId,
        @RequestHeader("ERIC-Authorised-User") String email, // TODO - extract email from header
        @PathVariable("company-number") final String companyNumber,
        @Valid @RequestBody final CreateDissolutionRequestDTO body,
        HttpServletRequest request) {

        if (StringUtils.isBlank(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (dissolutionService.doesDissolutionRequestExistForCompany(companyNumber)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        try {
            final CreateDissolutionResponseDTO response = dissolutionService.create(body, companyNumber, userId, request.getRemoteAddr(), email);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, List<String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(FieldError::getField))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().stream().map(FieldError::getDefaultMessage).collect(Collectors.toList())
                ));
    }
}
