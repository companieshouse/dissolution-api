package uk.gov.companieshouse.service.dissolution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.mapper.DissolutionRequestMapper;
import uk.gov.companieshouse.mapper.DissolutionResponseMapper;
import uk.gov.companieshouse.mapper.DissolutionUserDataMapper;
import uk.gov.companieshouse.model.db.dissolution.Dissolution;
import uk.gov.companieshouse.model.domain.DissolutionUserData;
import uk.gov.companieshouse.model.dto.companyOfficers.CompanyOfficer;
import uk.gov.companieshouse.model.dto.companyProfile.CompanyProfile;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateRequest;
import uk.gov.companieshouse.model.dto.dissolution.DissolutionCreateResponse;
import uk.gov.companieshouse.repository.DissolutionRepository;
import uk.gov.companieshouse.service.barcode.BarcodeGenerator;

import java.util.Map;

@Service
public class DissolutionCreator {

    private final ReferenceGenerator referenceGenerator;
    private final BarcodeGenerator barcodeGenerator;
    private final DissolutionRequestMapper requestMapper;
    private final DissolutionRepository repository;
    private final DissolutionResponseMapper responseMapper;
    private final DissolutionEmailService emailService;
    private final DissolutionUserDataMapper userDataMapper;

    @Autowired
    public DissolutionCreator(
            ReferenceGenerator referenceGenerator,
            BarcodeGenerator barcodeGenerator,
            DissolutionRequestMapper requestMapper,
            DissolutionRepository repository,
            DissolutionResponseMapper responseMapper,
            DissolutionEmailService emailService,
            DissolutionUserDataMapper userDataMapper) {
        this.referenceGenerator = referenceGenerator;
        this.barcodeGenerator = barcodeGenerator;
        this.requestMapper = requestMapper;
        this.repository = repository;
        this.responseMapper = responseMapper;
        this.emailService = emailService;
        this.userDataMapper = userDataMapper;
    }

    public DissolutionCreateResponse create(DissolutionCreateRequest body, CompanyProfile companyProfile, Map<String, CompanyOfficer> directors, String userId, String ip, String email) {
        final String reference = referenceGenerator.generateApplicationReference();
        final String barcode = barcodeGenerator.generateBarcode();
        final DissolutionUserData userData = userDataMapper.mapToUserData(userId, ip, email);

        final Dissolution dissolution = requestMapper.mapToDissolution(body, companyProfile, directors, userData, reference, barcode);

        repository.insert(dissolution);

        emailService.notifySignatoriesToSign(dissolution);

        return responseMapper.mapToDissolutionCreateResponse(dissolution);
    }
}
