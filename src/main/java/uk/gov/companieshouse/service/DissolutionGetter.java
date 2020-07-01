package uk.gov.companieshouse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.Dissolution;
import uk.gov.companieshouse.model.dto.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.DissolutionCreateResponse;
import uk.gov.companieshouse.model.dto.DissolutionGetResponse;
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

    public Optional<DissolutionGetResponse> get(String companyNumber) {
        return repository
                .findByCompanyNumber(companyNumber)
                .map(responseMapper::mapToDissolutionGetResponse);
    }
}
