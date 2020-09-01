package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.client.CompanyProfileClient;
import uk.gov.companieshouse.exception.BadRequestException;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.service.CompanyOfficerService;
import uk.gov.companieshouse.service.dissolution.validator.DissolutionValidator;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;

import static uk.gov.companieshouse.util.EricHelper.getEmail;

@RestController
@RequestMapping("/dissolution-request/{company-number}")
public class DissolutionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DissolutionController.class);

    private final DissolutionService dissolutionService;
    private final DissolutionValidator dissolutionValidator;
    private final CompanyProfileClient companyProfileClient;
    private final CompanyOfficerService companyOfficerService;

    public DissolutionController(
            DissolutionService dissolutionService,
            DissolutionValidator dissolutionValidator,
            CompanyProfileClient companyProfileClient,
            CompanyOfficerService companyOfficerService) {
        super();
        this.dissolutionService = dissolutionService;
        this.dissolutionValidator = dissolutionValidator;
        this.companyProfileClient = companyProfileClient;
        this.companyOfficerService = companyOfficerService;
    }

    @Operation(summary = "Create Dissolution Request", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dissolution Request created"),
            @ApiResponse(responseCode = "409", description = "Dissolution Request already exists for company", content = @Content),
            @ApiResponse(responseCode = "400", description = "Company Cannot Be Closed"),
            @ApiResponse(responseCode = "404", description = "Company not found")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public DissolutionCreateResponse submitDissolutionRequest(
            @RequestHeader("ERIC-identity") String userId,
            @RequestHeader("ERIC-Authorised-User") String authorisedUser,
            @PathVariable("company-number") final String companyNumber,
            @Valid @RequestBody final DissolutionCreateRequest body,
            HttpServletRequest request) {

        LOGGER.info("[POST] Submitting dissolution request for company number {}", companyNumber);

        final CompanyProfile company = Optional
                .ofNullable(companyProfileClient.getCompanyProfile(companyNumber))
                .orElseThrow(NotFoundException::new);

        if (dissolutionService.doesDissolutionRequestExistForCompanyByCompanyNumber(companyNumber)) {
            throw new ConflictException("Dissolution already exists");
        }

        final Map<String, CompanyOfficer> directors = companyOfficerService.getActiveDirectorsForCompany(companyNumber);

        dissolutionValidator
                .checkBusinessRules(company, directors, body.getDirectors())
                .ifPresent(error -> { throw new BadRequestException(error); });

        return dissolutionService.create(body, company, directors, userId, request.getRemoteAddr(), getEmail(authorisedUser));
    }

    @Operation(summary = "Get Dissolution Application", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dissolution Application found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dissolution Application not found")
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public DissolutionGetResponse getDissolutionApplication(@PathVariable("company-number") final String companyNumber) {

        LOGGER.info("[GET] Getting dissolution info for company number {}", companyNumber);

        return dissolutionService
                .getByCompanyNumber(companyNumber)
                .orElseThrow(NotFoundException::new);
    }

    @Operation(summary = "Patch Dissolution Application", tags = "Dissolution")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dissolution Application patched", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dissolution Application not found"),
            @ApiResponse(responseCode = "400", description = "Dissolution Request does not have a director pending approval")
    })
    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public DissolutionPatchResponse patchDissolutionApplication(
            @RequestHeader("ERIC-identity") String userId,
            @PathVariable("company-number") final String companyNumber,
            @Valid @RequestBody final DissolutionPatchRequest body,
            HttpServletRequest request) {

        LOGGER.info("[PATCH] Updating dissolution info for company number {}", companyNumber);

        if (!dissolutionService.doesDissolutionRequestExistForCompanyByCompanyNumber(companyNumber)) {
            throw new NotFoundException();
        }

        if (!dissolutionService.isDirectorPendingApproval(companyNumber, body.getOfficerId())) {
            throw new BadRequestException("Director is not pending approval");
        }

        return dissolutionService.addDirectorApproval(companyNumber, userId, request.getRemoteAddr(), body.getOfficerId());
    }
}
