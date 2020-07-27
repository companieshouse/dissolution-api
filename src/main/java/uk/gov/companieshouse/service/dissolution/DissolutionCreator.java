package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;

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

    public DissolutionCreateResponse create(DissolutionCreateRequest body, CompanyProfileApi companyProfileApi, String userId, String ip, String email) {
        final String reference = referenceGenerator.generateApplicationReference();

        final Dissolution dissolution = requestMapper.mapToDissolution(body, companyProfileApi, userId, email, ip, reference);

        repository.insert(dissolution);

        return responseMapper.mapToDissolutionCreateResponse(dissolution);
    }
}
