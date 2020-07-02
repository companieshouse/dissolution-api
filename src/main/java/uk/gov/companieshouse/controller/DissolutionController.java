package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.UnauthorisedException;
import uk.gov.companieshouse.model.dto.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.DissolutionGetResponse;
import uk.gov.companieshouse.service.DissolutionService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static uk.gov.companieshouse.util.EricHelper.getEmail;

@RestController
@RequestMapping("/dissolution-request/{company-number}")
public class DissolutionController {

    private final DissolutionService dissolutionService;
    private final Logger logger = LoggerFactory.getLogger(DissolutionController.class);

    public DissolutionController(DissolutionService dissolutionService) {
        super();
        this.dissolutionService = dissolutionService;
    }

    @Operation(summary = "Create Dissolution Request", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dissolution Request created"),
            @ApiResponse(responseCode = "409", description = "Dissolution Request already exists for company", content = @Content),
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DissolutionCreateResponse submitDissolutionRequest(
            @RequestHeader("ERIC-identity") String userId,
            @RequestHeader("ERIC-Authorised-User") String authorisedUser,
            @PathVariable("company-number") final String companyNumber,
            @Valid @RequestBody final DissolutionCreateRequest body,
            HttpServletRequest request) {

        if (StringUtils.isBlank(userId)) {
            throw new UnauthorisedException();
        }

        if (dissolutionService.doesDissolutionRequestExistForCompany(companyNumber)) {
            throw new ConflictException();
        }

        logger.debug("[POST] Submitting dissolution request for company number {}", companyNumber);

        return dissolutionService.create(body, companyNumber, userId, request.getRemoteAddr(), getEmail(authorisedUser));
    }

    @Operation(summary = "Get Dissolution Application", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dissolution Application found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dissolution Application not found")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public DissolutionGetResponse getDissolutionApplication(
            @RequestHeader("ERIC-identity") String userId,
            @PathVariable("company-number") final String companyNumber) {

        if (StringUtils.isBlank(userId)) {
            throw new UnauthorisedException();
        }

        logger.debug("[GET] Getting dissolution info for company number {}", companyNumber);

        return dissolutionService
                .get(companyNumber)
                .orElseThrow(DissolutionNotFoundException::new);
    }
}
