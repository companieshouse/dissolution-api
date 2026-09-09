package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionGetResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

import java.util.Optional;

@Service
public class DissolutionGetter {

    private final DissolutionRepository repository;
    private final DissolutionResponseMapper responseMapper;

    @Autowired
    public DissolutionGetter(
            DissolutionRepository repository,
            DissolutionResponseMapper responseMapper) {
        this.repository = repository;
        this.responseMapper = responseMapper;
    }

    public Optional<DissolutionGetResponse> getByApplicationReference(String applicationReference) {
        return repository
                .findByDataApplicationReference(applicationReference)
                .map(responseMapper::mapToDissolutionGetResponse);
    }
}
