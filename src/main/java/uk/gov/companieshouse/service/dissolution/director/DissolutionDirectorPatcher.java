package uk.gov.companieshouse.service.dissolution.director;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.exception.DissolutionNotFoundException;
import uk.gov.companieshouse.mapper.DissolutionDirectorResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.db.dissolution.DissolutionDirector;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionDirectorPatchResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.dissolution.DissolutionEmailService;

import java.util.Objects;

@Service
public class DissolutionDirectorPatcher {

    private final DissolutionRepository repository;
    private final DissolutionEmailService dissolutionEmailService;
    private final DissolutionDirectorResponseMapper directorResponseMapper;

    @Autowired
    public DissolutionDirectorPatcher(
            DissolutionRepository repository,
            DissolutionEmailService dissolutionEmailService,
            DissolutionDirectorResponseMapper directorResponseMapper
    ) {
        this.repository = repository;
        this.dissolutionEmailService = dissolutionEmailService;
        this.directorResponseMapper = directorResponseMapper;
    }

    private DissolutionDirector findDirector(String officerId, Dissolution dissolution) throws DissolutionNotFoundException {
        return dissolution
                .getData()
                .getDirectors()
                .stream()
                .filter(director -> director.getOfficerId().equals(officerId))
                .findFirst()
                .orElseThrow(DissolutionNotFoundException::new);
    }

    public DissolutionDirectorPatchResponse updateSignatory(String companyNumber, DissolutionDirectorPatchRequest body, String officerId) throws DissolutionNotFoundException {
        final Dissolution dissolution = this.repository.findByCompanyNumber(companyNumber).orElseThrow(DissolutionNotFoundException::new);

        this.updateSignatory(body, dissolution, officerId);

        return this.directorResponseMapper.mapToDissolutionDirectorPatchResponse(dissolution);
    }

    private void updateSignatory(DissolutionDirectorPatchRequest body, Dissolution dissolution, String officerId) throws DissolutionNotFoundException {
        DissolutionDirector director = this.findDirector(officerId, dissolution);

        if (hasDataChanged(director, body)) {

            director.setEmail(body.getEmail());
            director.setOnBehalfName(body.getOnBehalfName());

            this.repository.save(dissolution);

            dissolutionEmailService.notifySignatoryToSign(dissolution, director.getEmail());
        }
    }

    private boolean hasDataChanged(DissolutionDirector director, DissolutionDirectorPatchRequest body) {
        return !director.getEmail().equals(body.getEmail()) || !Objects.equals(director.getOnBehalfName(), body.getOnBehalfName());
    }
}
