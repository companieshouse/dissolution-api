package uk.gov.companieshouse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.exception.BadRequestException;
import uk.gov.companieshouse.exception.ConflictException;
import uk.gov.companieshouse.model.domain.DissolutionDirectorApprovalData;
import uk.gov.companieshouse.model.dto.companyprofile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateDraftResponse;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionPatchRequest;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;
import uk.gov.companieshouse.service.CompanyProfileService;
import uk.gov.companieshouse.service.dissolution.DissolutionService;
import uk.gov.companieshouse.service.dissolution.validator.TransactionValidator;

import static uk.gov.companieshouse.model.Constants.COMPANY_NUMBER_KEY;
import static uk.gov.companieshouse.model.Constants.TRANSACTION_KEY;
import static uk.gov.companieshouse.util.EricHelper.getEmail;

@SuppressWarnings("UastIncorrectHttpHeaderInspection")
@RestController
@Tag(name = "Dissolution (Transaction Model)", description = "Endpoints for managing dissolution requests via the transaction model")
@RequestMapping("/company/{company-number}/transaction/{transaction_id}/dissolution")
public class TransactionsDissolutionController {

    private final DissolutionService dissolutionService;
    private final CompanyProfileService companyProfileService;

    public TransactionsDissolutionController(
            DissolutionService dissolutionService,
            CompanyProfileService companyProfileService) {
        this.dissolutionService = dissolutionService;
        this.companyProfileService = companyProfileService;
    }

    @Operation(summary = "Create Draft Dissolution Request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Draft Dissolution created"),
            @ApiResponse(responseCode = "400", description = "Company Cannot Be Closed"),
            @ApiResponse(responseCode = "404", description = "Company not found"),
            @ApiResponse(responseCode = "409", description = "Transaction is not open, is not associated with the company or draft Dissolution already exists", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DissolutionCreateDraftResponse submitDraftDissolution(
            @RequestHeader("ERIC-identity") String userId,
            @RequestHeader("ERIC-Authorised-User") String authorisedUser,
            @PathVariable(COMPANY_NUMBER_KEY) final String companyNumber,
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            HttpServletRequest request) {

        final var validator = new TransactionValidator(transaction);

        if (!validator.hasStatus(TransactionStatus.OPEN)) {
            throw new ConflictException("Transaction is already closed or closed pending payment");
        }

        if (!validator.isLinkedToCompany(companyNumber)) {
            throw new ConflictException("Transaction does not belong to company " + companyNumber);
        }

        if (dissolutionService.doesDraftDissolutionExistForUserAndCompany(userId, companyNumber)) {
            throw new ConflictException("Draft dissolution already exists for user " + userId);
        }

        final CompanyProfile company = companyProfileService.getCompanyProfile(companyNumber, request.getHeader(ApiSdkManager.getEricPassthroughTokenHeader()));

        if (!companyProfileService.isCompanyClosable(company)) {
            throw new BadRequestException("Company must be of a closable type, have an active status and must not be an overseas company");
        }

        return dissolutionService.createDraft(transaction, company, userId, request.getRemoteAddr(), getEmail(authorisedUser));
    }

    @Operation(summary = "Patch Dissolution Application Approval")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dissolution Application successfully endorsed", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dissolution Request director is not a signatory or has already approved"),
            @ApiResponse(responseCode = "404", description = "Dissolution Application or Company not found"),
            @ApiResponse(responseCode = "409", description = "Transaction is not open, is not associated with the company", content = @Content)
    })
    @PatchMapping("/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patchDissolutionApproval(
            @RequestHeader("ERIC-identity") String userId,
            @PathVariable(COMPANY_NUMBER_KEY) final String companyNumber,
            @RequestAttribute(TRANSACTION_KEY) Transaction transaction,
            @Valid @RequestBody final DissolutionPatchRequest patchRequest
    ) {
        final var validator = new TransactionValidator(transaction);

        if (!validator.hasStatus(TransactionStatus.OPEN)) {
            throw new ConflictException("Transaction is already closed or closed pending payment");
        }

        if (!validator.isLinkedToCompany(companyNumber)) {
            throw new ConflictException("Transaction does not belong to company " + companyNumber);
        }

        final var directorApprovalData = new DissolutionDirectorApprovalData(userId, patchRequest.getOfficerId(), patchRequest.getIpAddress(), patchRequest.getHasApproved());
        dissolutionService.addDirectorApproval(companyNumber, directorApprovalData);
    }
}
