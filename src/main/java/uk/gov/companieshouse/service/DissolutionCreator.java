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
public class DissolutionCreator {

    private final ReferenceGenerator referenceGenerator;
    private final DissolutionRequestMapper requestMapper;
    private final DissolutionRepository repository;
    private final DissolutionResponseMapper responseMapper;

    @Autowired
    public DissolutionCreator(
        ReferenceGenerator referenceGenerator,
        DissolutionRequestMapper requestMapper,
        DissolutionRepository repository,
        DissolutionResponseMapper responseMapper) {
        this.referenceGenerator = referenceGenerator;
        this.requestMapper = requestMapper;
        this.repository = repository;
        this.responseMapper = responseMapper;
    }

    public DissolutionCreateResponse create(DissolutionCreateRequest body, String companyNumber, String userId, String ip, String email) {
        final String reference = referenceGenerator.generateApplicationReference();

        final Dissolution dissolution = requestMapper.mapToDissolution(body, companyNumber, userId, email, ip, reference);

        repository.insert(dissolution);

        return responseMapper.mapToDissolutionCreateResponse(dissolution);
    }

    public DissolutionGetResponse get(String companyNumber) {
        Optional<Dissolution> dissolution = repository.findByCompanyNumber(companyNumber);

        return responseMapper.mapToDissolutionGetResponse(dissolution.get());
    }
}
