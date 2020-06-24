package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.UnauthorisedException;
import uk.gov.companieshouse.model.dto.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.service.DissolutionService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static uk.gov.companieshouse.util.EricHelper.getEmail;

@RestController
@RequestMapping("/dissolution-request/{company-number}")
public class DissolutionController {

    private final DissolutionService dissolutionService;

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

        return dissolutionService.create(body, companyNumber, userId, request.getRemoteAddr(), getEmail(authorisedUser));
    }
}
