package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.exception.BadRequestException;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.model.dto.companyofficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchResponse;
import uk.gov.companieshouse.model.enums.ApplicationStatus;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;
import uk.gov.companieshouse.service.CompanyOfficerService;
import uk.gov.companieshouse.client.CompanyProfileClient;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.DissolutionValidator;
import uk.gov.companieshouse.service.payment.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;

import static uk.gov.companieshouse.util.EricHelper.getEmail;

@RestController
@RequestMapping("/dissolution-request/{company-number}")
public class DissolutionController {

    private final DissolutionService dissolutionService;
    private final DissolutionValidator dissolutionValidator;
    private final CompanyProfileClient companyProfileClient;
    private final CompanyOfficerService companyOfficerService;
    private final PaymentService paymentService;
    private final Logger logger;

    public DissolutionController(
            DissolutionService dissolutionService,
            DissolutionValidator dissolutionValidator,
            CompanyProfileClient companyProfileClient,
            CompanyOfficerService companyOfficerService,
            PaymentService paymentService,
            Logger logger) {
        this.dissolutionService = dissolutionService;
        this.dissolutionValidator = dissolutionValidator;
        this.companyProfileClient = companyProfileClient;
        this.companyOfficerService = companyOfficerService;
        this.paymentService = paymentService;
        this.logger = logger;
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

        final CompanyProfileApi companyProfileApi = Optional
                .ofNullable(companyProfileClient.getCompanyProfile(companyNumber, request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader())))
                .orElseThrow(NotFoundException::new);

        CompanyProfile company = new CompanyProfile.Builder()
                .withCompanyName(companyProfileApi.getCompanyName())
                .withType(companyProfileApi.getType())
                .withCompanyNumber(companyProfileApi.getCompanyNumber())
                .withCompanyStatus(companyProfileApi.getCompanyStatus())
                .build();


        if (dissolutionService.doesDissolutionRequestExistForCompanyByCompanyNumber(companyNumber)) {
            throw new ConflictException("Dissolution already exists");
        }

        final Map<String, CompanyOfficer> directors = companyOfficerService.getActiveDirectorsForCompany(companyNumber);

        dissolutionValidator
                .checkBusinessRules(company, directors, body.getDirectors())
                .ifPresent(error -> {
                    throw new BadRequestException(error);
                });

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
        DissolutionGetResponse dissolutionGetResponse = dissolutionService
                .getByCompanyNumber(companyNumber)
                .orElseThrow(NotFoundException::new);
        String paymentRef = dissolutionGetResponse.getPaymentReference();
        if ( paymentRef != null && !paymentRef.equals("") && dissolutionGetResponse.getApplicationStatus().equals(ApplicationStatus.PENDING_PAYMENT)) {
            // payment could be complete, we need to get up-to-date status to be sure
            String paymentStatus = paymentService.getPaymentStatus(dissolutionGetResponse.getPaymentReference());
            if (paymentStatus == null) {
                logger.info(String.format("Error getting payment status for paymentRef: [%s], resetting payment ref", paymentRef));
                // error retrieving payment status, so reset payment reference to allow user to restart payment
                try {
                    dissolutionService.setPaymentReference("", dissolutionGetResponse.getApplicationReference());
                } catch (DissolutionNotFoundException e) {
                    throw new NotFoundException();
                }
            } else if (paymentStatus.equals("accepted")) {
                dissolutionGetResponse.setApplicationStatus(ApplicationStatus.PAID);
            }
        }
        return dissolutionGetResponse;
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
            @Valid @RequestBody final DissolutionPatchRequest body
    ) {

        if (!dissolutionService.doesDissolutionRequestExistForCompanyByCompanyNumber(companyNumber)) {
            throw new NotFoundException();
        }

        if (!dissolutionService.isDirectorPendingApproval(companyNumber, body.getOfficerId())) {
            throw new BadRequestException("Director is not pending approval");
        }

        try {
            return dissolutionService.addDirectorApproval(companyNumber, userId, body);
        } catch (DissolutionNotFoundException e) {
            throw new NotFoundException();
        }
    }
}
