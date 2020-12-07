package uk.gov.companieshouse.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.exception.BadRequestException;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.exception.NotFoundException;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.service.dissolution.DissolutionService;

import javax.validation.Valid;
import java.util.Optional;

import static uk.gov.companieshouse.util.EricHelper.getEmail;

@RestController
@RequestMapping("/dissolution-request/{company-number}/directors/{director-id}")
public class DissolutionDirectorController {
    private final DissolutionService dissolutionService;

    public DissolutionDirectorController(DissolutionService dissolutionService) {
        super();
        this.dissolutionService = dissolutionService;
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public DissolutionDirectorPatchResponse patchDissolutionDirector(
            @RequestHeader("ERIC-Authorised-User") String authorisedUser,
            @PathVariable("company-number") final String companyNumber,
            @PathVariable("director-id") final String directorId,
            @Valid @RequestBody final DissolutionDirectorPatchRequest body
    ) throws DissolutionNotFoundException {

        if (!dissolutionService.doesDirectorExist(companyNumber, directorId)) {
            throw new NotFoundException();
        }

        final Optional<String> error = dissolutionService.checkPatchDirectorConstraints(companyNumber, directorId, getEmail(authorisedUser));
        if (error.isPresent()) {
            throw new BadRequestException(error.get());
        }

        try {
            return dissolutionService.updateSignatory(companyNumber, body, directorId);
        } catch (DissolutionNotFoundException e) {
            throw new NotFoundException();
        }
    }
}
